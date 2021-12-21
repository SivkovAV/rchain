package coop.rchain.rspace.state

import cats.effect._
import cats.syntax.all._
import coop.rchain.rspace.hashing.Blake2b256Hash
import coop.rchain.state.TrieImporter
import fs2.Stream
import scodec.bits.ByteVector

trait RSpaceImporter[F[_]] extends TrieImporter[F] {
  type KeyHash = Blake2b256Hash

  def getHistoryItem(hash: KeyHash): F[Option[ByteVector]]
}

final case class StateValidationError(message: String) extends Exception(message) {
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def this(message: String, cause: Throwable) = {
    this(message)
    initCause(cause)
  }
}

object RSpaceImporter {
  def validateStateItems[F[_]: Concurrent, C, P, A, K](
      historyItems: Seq[(Blake2b256Hash, ByteVector)],
      dataItems: Seq[(Blake2b256Hash, ByteVector)],
      startPath: Seq[(Blake2b256Hash, Option[Byte])],
      chunkSize: Int,
      skip: Int,
      getFromHistory: Blake2b256Hash => F[Option[ByteVector]]
  )(implicit sc: Serialize[C], sp: Serialize[P], sa: Serialize[A], sk: Serialize[K]): F[Unit] = {
    import cats.instances.list._

    val receivedHistorySize = historyItems.size
    val isEnd               = receivedHistorySize < chunkSize

    def raiseError[T](msg: String): F[T] = new StateValidationError(msg).raiseError[F, T]
    def raiseErrorEx[T](msg: String, cause: Throwable): F[T] =
      new StateValidationError(msg, cause).raiseError[F, T]

    def decodeData(bytes: ByteVector) =
      ColdStoreInstances.codecPersistedData.decodeValue(bytes.bits).toEitherThrowable

    // Validate history items size
    def validateHistorySize[T]: F[Unit] = {
      val sizeIsValid = receivedHistorySize == chunkSize | isEnd
      raiseError[T](
        s"Input size of history items is not valid. Expected chunk size $chunkSize, received $receivedHistorySize."
      ).whenA(!sizeIsValid)
    }

    // Validate history hashes
    def validateHistoryItemsHashes: F[List[(ByteVector, ByteVector)]] =
      historyItems.toList traverse {
        case (hash, trieBytes) =>
          val trieHash = Blake2b256Hash.create(trieBytes)
          if (hash == trieHash) (trieHash.bytes, trieBytes).pure[F]
        //    but on each case of Trie instance (see `Trie.hash`)
        //  - this adds substantial time for validation e.g. 20k records 450ms (with encoding 2.4sec)
        // https://github.com/rchain/rchain/blob/4dd216a7/rspace/src/main/scala/coop/rchain/rspace/history/HistoryStore.scala#L25-L26
        val trieHash = Trie.hash(trie)
          else
            raiseError(
              s"Trie hash does not match decoded trie, key: ${hash.bytes.toHex}, decoded: ${trieHash.bytes.toHex}."
            )
      }

    // Validate data hashes
    def validateDataItemsHashes =
      Stream
        .emits(dataItems)
        .covary[F]
        .parEvalMapUnordered(64) {
          case (hash, valueBytes) =>
            for {
              persistedData <- decodeData(valueBytes).liftTo.handleErrorWith { ex: Throwable =>
                                raiseErrorEx(s"Decode value of key ${hash.bytes.toHex} failed.", ex)
                              }
              dataHash = Blake2b256Hash.create(persistedData.bytes)
              _ <- if (hash == dataHash) {
                    // When hash matches we also need to check if variance of PersistedData if correct
                    //  because we don't have the hash of concrete type holding hashed binary value.
                    Sync[F]
                      .delay {
                        persistedData match {
                          case JoinsLeaf(bytes)         => decodeJoins[C](bytes)
                          case DataLeaf(bytes)          => decodeDatums[A](bytes)
                          case ContinuationsLeaf(bytes) => decodeContinuations[P, K](bytes)
                        }
                      }
                      .void
                      .handleErrorWith { ex: Throwable =>
                        raiseErrorEx(
                          s"Decode inner class of key: ${hash.bytes.toHex} failed for ${persistedData.getClass.getSimpleName}.",
                          ex
                        )
                      }
                  } else
                    raiseError[Unit](
                      s"Data hash does not match decoded data, key: ${hash.bytes.toHex}, decoded: ${dataHash.bytes.toHex}."
                    )
            } yield ()
        }

    def trieHashNotFoundError(h: Blake2b256Hash) =
      new StateValidationError(
        s"Trie hash not found in received items or in history store, hash: ${h.bytes.toHex}."
      )

    // Find Trie by hash. Trie must be found in received history items or in previously imported items.
    def getTrie(st: Map[ByteVector, ByteVector])(hash: ByteVector) = {
      val trieOpt = st.get(hash)
      trieOpt.fold {
        for {
          bytesOpt <- getFromHistory(Blake2b256Hash.fromByteArray(hash.toArray))
          _        <- bytesOpt.liftTo(trieHashNotFoundError(Blake2b256Hash.fromByteArray(hash.toArray)))
        } yield bytesOpt
      }(_.some.pure[F])
    }

    for {
      // Validate chunk size.
      _ <- validateHistorySize

      // Validate tries from received history items.
      trieMap <- validateHistoryItemsHashes map (_.toMap)

      // Traverse trie and extract nodes / the same as in export. Nodes must match hashed keys.
      nodes <- RSpaceExporter.traverseTrie(startPath, skip, chunkSize, getTrie(trieMap))

      // Extract history and data keys.
      (leafs, nonLeafs) = nodes.partition(_.isLeaf)
      historyKeys       = nonLeafs.map(_.hash)
      dataKeys          = leafs.map(_.hash)

      // Validate keys / cryptographic proof that store chunk is not corrupted or modified.
      historyKeysMatch = historyItems.map(_._1) == historyKeys
      _                <- raiseError(s"History items are corrupted.").whenA(!historyKeysMatch)

      dataKeysMatch = dataItems.map(_._1) == dataKeys
      _             <- raiseError(s"Data items are corrupted.").whenA(!dataKeysMatch)

      // Validate data (leaf) items hashes. It's the last check because it's the heaviest.
      _ <- validateDataItemsHashes.compile.drain
    } yield ()
  }
}
