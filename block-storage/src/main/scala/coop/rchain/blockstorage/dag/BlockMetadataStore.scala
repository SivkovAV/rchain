package coop.rchain.blockstorage.dag

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import cats.{Monad, Show}
import coop.rchain.casper.PrettyPrinter
import coop.rchain.models.BlockHash.BlockHash
import coop.rchain.models.BlockMetadata
import coop.rchain.shared.Log
import coop.rchain.shared.syntax._
import coop.rchain.store.KeyValueTypedStore

import scala.collection.immutable.SortedMap

object BlockMetadataStore {
  def apply[F[_]: Sync: Log](
      blockMetadataStore: KeyValueTypedStore[F, BlockHash, BlockMetadata]
  ): F[BlockMetadataStore[F]] =
    for {
      _ <- Log[F].info("Building in-memory blockMetadataStore.")
      // Iterate over block metadata store and collect info for in-memory cache
      blockInfoMap <- blockMetadataStore.collect {
                       case (hash, metaData) =>
                         (hash, blockMetadataToInfo(metaData()))
                     }
      _           <- Log[F].info("Reading data from blockMetadataStore done.")
      dagState    = recreateInMemoryState(blockInfoMap.toMap)
      _           <- Log[F].info("Successfully built in-memory blockMetadataStore.")
      dagStateRef <- Ref[F].of(dagState)
    } yield new BlockMetadataStore[F](blockMetadataStore, dagStateRef)

  final case class BlockMetadataStoreInconsistencyError(message: String) extends Exception(message)

  private final case class DagState(
      dagSet: Set[BlockHash],
      childMap: Map[BlockHash, Set[BlockHash]],
      heightMap: SortedMap[Long, Set[BlockHash]],
      // In general - at least genesis should be LFB.
      // But dagstate can be empty, as it is initialized before genesis is inserted.
      // Also lots of tests do not have genesis properly initialised, so fixing all this is pain.
      // So this is Option.
      lastFinalizedBlock: Option[(BlockHash, Long)],
      finalizedBlockSet: Set[BlockHash]
  )

  def blockMetadataToInfo(blockMeta: BlockMetadata): BlockInfo =
    BlockInfo(
      blockMeta.blockHash,
      // TODO: tempo fix to support removal of parents from BlockMessage
      blockMeta.parents.toSet ++ blockMeta.justifications,
      blockMeta.blockNum,
      blockMeta.invalid,
      blockMeta.finalized
    )

  class BlockMetadataStore[F[_]: Monad](
      private val store: KeyValueTypedStore[F, BlockHash, BlockMetadata],
      private val dagState: Ref[F, DagState]
  ) {
    def add(block: BlockMetadata): F[Unit] =
      for {
        // Update DAG state with new block
        _ <- dagState.update { st =>
              val blockInfo   = blockMetadataToInfo(block)
              val newDagState = addBlockToDagState(blockInfo)(st)
              validateDagState(newDagState)
            }

        // Update persistent block metadata store
        _ <- store.put(block.blockHash, block)
      } yield ()

    /** Record new last finalized lock. Directly finalized is the output of finalizer,
      * indirectly finalized are new LFB ancestors. */
    def recordFinalized(directly: BlockHash, indirectly: Set[BlockHash])(
        implicit sync: Sync[F]
    ): F[Unit] = {
      implicit val s = new Show[BlockHash] {
        override def show(t: BlockHash): String = PrettyPrinter.buildString(t)
      }

      for {
        // read current values
        curMetaForDF  <- store.getUnsafe(directly)
        curMetasForIF <- store.getUnsafeBatch(indirectly.toList)
        // new values to persist
        newMetaForDF  = (directly, curMetaForDF.copy(finalized = true))
        newMetasForIF = curMetasForIF.map(v => (v.blockHash, v.copy(finalized = true)))
        // update in memory state
        _ <- dagState.update { st =>
              val newFinalizedSet = st.finalizedBlockSet ++ indirectly + directly

              // update lastFinalizedBlock only when current one is lower
              if (st.lastFinalizedBlock.exists { case (_, h) => h > curMetaForDF.blockNum })
                st.copy(finalizedBlockSet = newFinalizedSet)
              else
                st.copy(
                  finalizedBlockSet = newFinalizedSet,
                  lastFinalizedBlock = (directly, curMetaForDF.blockNum).some
                )
            }
        // persist new values all at once
        _ <- store.put(newMetaForDF +: newMetasForIF)
      } yield ()
    }

    def get(hash: BlockHash): F[Option[BlockMetadata]] = store.get1(hash)

    def getUnsafe(hash: BlockHash)(
        implicit f: Sync[F],
        line: sourcecode.Line,
        file: sourcecode.File,
        enclosing: sourcecode.Enclosing
    ): F[BlockMetadata] = {
      def source = s"${file.value}:${line.value} ${enclosing.value}"
      def errMsg =
        s"BlockMetadataStore is missing key ${PrettyPrinter.buildString(hash)}\n  $source"
      get(hash) >>= (_.liftTo(BlockMetadataStoreInconsistencyError(errMsg)))
    }

    // DAG state operations

    def dagSet: F[Set[BlockHash]] = dagState.get.map(_.dagSet)

    def contains(hash: BlockHash): F[Boolean] = dagState.get.map(_.dagSet.contains(hash))

    def childMapData: F[Map[BlockHash, Set[BlockHash]]] =
      dagState.get.map(_.childMap)

    def heightMap: F[SortedMap[Long, Set[BlockHash]]] =
      dagState.get.map(_.heightMap)

    // This is Option because method is called on BlockDagStorage initialization which is before
    // the first finalized block (genesis) is inserted.
    def lastFinalizedBlock(implicit sync: Sync[F]): F[Option[BlockHash]] =
      dagState.get.map(_.lastFinalizedBlock.map(_._1))

    def finalizedBlockSet: F[Set[BlockHash]] = dagState.get.map(_.finalizedBlockSet)
  }

  private def addBlockToDagState(block: BlockInfo)(state: DagState): DagState = {
    // Update dag set / all blocks in the DAG
    val newDagSet = state.dagSet + block.hash

    // Update children relation map
    val blockChilds = block.parents.map((_, Set(block.hash))) + ((block.hash, Set()))
    val newChildMap = blockChilds.foldLeft(state.childMap) {
      case (acc, (key, newChildren)) =>
        val currChildren = acc.getOrElse(key, Set.empty[BlockHash])
        acc.updated(key, currChildren ++ newChildren)
    }

    // Update block height map
    val newHeightMap = if (!block.isInvalid) {
      val currSet = state.heightMap.getOrElse(block.blockNum, Set())
      state.heightMap.updated(block.blockNum, currSet + block.hash)
    } else state.heightMap

    val newLastFinalizedBlock =
      if (block.isFinalized &&
          !state.lastFinalizedBlock.exists { case (_, height) => height > block.blockNum })
        (block.hash, block.blockNum).some
      else state.lastFinalizedBlock

    val newFinalisedBlockSet =
      if (block.isFinalized) state.finalizedBlockSet + block.hash else state.finalizedBlockSet

    state.copy(
      dagSet = newDagSet,
      childMap = newChildMap,
      heightMap = newHeightMap,
      lastFinalizedBlock = newLastFinalizedBlock,
      finalizedBlockSet = newFinalisedBlockSet
    )
  }

  private def validateDagState(state: DagState): DagState = {
    // Validate height map index (block numbers) are in sequence without holes
    val m          = state.heightMap
    val (min, max) = if (m.nonEmpty) (m.firstKey, m.lastKey + 1) else (0L, 0L)
    assert(max - min == m.size.toLong, "DAG store height map has numbers not in sequence.")
    state
  }

  // Used to project part of the block metadata for in-memory initialization
  final case class BlockInfo(
      hash: BlockHash,
      parents: Set[BlockHash],
      blockNum: Long,
      isInvalid: Boolean,
      isFinalized: Boolean
  )

  private def recreateInMemoryState(
      blocksInfoMap: Map[BlockHash, BlockInfo]
  ): DagState = {
    val emptyState: DagState =
      DagState(
        dagSet = Set(),
        childMap = Map(),
        heightMap = SortedMap(),
        lastFinalizedBlock = none[(BlockHash, Long)],
        finalizedBlockSet = Set()
      )

    // Add blocks to DAG state
    val dagState = blocksInfoMap.foldLeft(emptyState) {
      case (state, (_, block)) => addBlockToDagState(block)(state)
    }

    validateDagState(dagState)
  }
}
