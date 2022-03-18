package coop.rchain.node.mergeablity

import coop.rchain.node.mergeablity.OperationOn0Ch._
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class PeekMergeability extends FlatSpec with Matchers with Inspectors with BasicMergeabilityRules {
  // it should"PX !X"    in ConflictingCase(S0)(P_)(Nil)(S0.rstate)(P_.rstate) // non deteministic, depends on which exactly produce is commed
  it should "PX !4" in MergeableCase(P1)(F_)(S0)(P1.rstate)
  it should "PX (!4)" in MergeableCase(P1)(F_, S0)(Nil)(P1.rstate)
  it should "PX !C" in MergeableCase(P1)(C_)(S0)(P1.rstate ++ C_.rstate)
  it should "PX (!C)" in MergeableCase(P1)(C_, S0)(Nil)(P1.rstate ++ C_.rstate)
  it should "PX 4X" in MergeableCase(F_)(P_)(Nil)(F_.rstate ++ P_.rstate)
  it should "PX 4!" in MergeableCase(F_)(P1)(S0)(P1.rstate)
  it should "PX (4!)" in CoveredBy("PX (!4)")
  it should "PX 4!!" in CurrentConflictMergeableCase(P0)(R1)(F1)(P0.rstate ++ F1.rstate)(R1.rstate)
  it should "PX (4!!)" in CurrentConflictMergeableCase(P0)(R1, F1)(Nil)(P0.rstate)(R1.rstate)
  it should "PX PX" in MergeableCase(P_)(P_)(Nil)(P_.rstate ++ P_.rstate)
  it should "PX P!" in CurrentConflictMergeableCase(P0)(S1)(P_)(P0.rstate ++ P_.rstate)(S1.rstate)
  it should "PX (P!)" in ConflictingCase(P_)(P_, S0)(Nil)(P_.rstate)(S0.rstate)
  it should "PX !P" in MergeableCase(P0)(P_)(S1)(P0.rstate ++ S1.rstate)
  it should "PX (!P)" in CoveredBy("PX (P!)")
  it should "PX P!!" in CurrentConflictMergeableCase(P1)(R0)(P_)(P1.rstate ++ P_.rstate)(R0.rstate)
  it should "PX (P!!)" in ConflictingCase(P_)(P_, R0)(Nil)(P_.rstate)(R0.rstate)
  it should "PX !!P" in MergeableCase(P1)(P_)(R0)(P1.rstate ++ R0.rstate)
  it should "PX !!X" in CurrentConflictMergeableCase(P0)(R1)(Nil)(P0.rstate)(R1.rstate)
  it should "PX !!X 2" in ConflictingCase(P_)(R1)(Nil)(P_.rstate)(R1.rstate)
  it should "PX !!4" in MergeableCase(Nil)(F_)(P0, R1)(P0.rstate ++ R1.rstate)
  it should "PX (!!4)" in CurrentConflictMergeableCase(P0)(F_, R1)(Nil)(P0.rstate)(R1.rstate)
  // it should  "PX !!C"   in InfiniteLoop(P1)(C0)(R0)(P1.rstate ++ R0.rstate)
  // it should  "PX (!!C)" in InfiniteLoop(P1)(C0, R0)(Nil)(P1.rstate)
  it should "PX CX" in MergeableCase(P_)(C_)(Nil)(P_.rstate ++ C_.rstate)
  it should "PX C!" in MergeableCase(P0)(C_)(S1)(P0.rstate ++ C_.rstate)
  it should "PX (C!)" in MergeableCase(P_)(C_, S0)(Nil)(P_.rstate ++ C_.rstate)
  // it should  "PX C!!"   in InfiniteLoop(P1)(R0)(C0)(P1.rstate ++ C0.rstate)
  it should "PX C!!" in CoveredBy("PX (!!C)")
  it should "P! !X" in MergeableCase(S0)(P_)(S0)(S0.rstate ++ S0.rstate)
  it should "(P!) !X" in MergeableCase(S0)(P_, S0)(Nil)(S0.rstate ++ S0.rstate)
  it should "!P !X" in MergeableCase(S0)(S1)(P1)(S0.rstate ++ S1.rstate)
  it should "(!P) !X" in CoveredBy("(P!) !X")
  it should "P! !4" in MergeableCase(S1)(F0)(S0, P1)(S1.rstate)
  // it should  "(P!) !4"  in NonDeterminedCase(P_, S1)(F0)(S0)(emptyState)(emptyState)
  it should "(P!) !4 2" in MergeableCase(P1, S1)(F0)(S0)(S1.rstate)
  // it should  "P! (!4)"   in NonDeterminedUnknownCase(S0, F0)(S1)(P_)(emptyState)(emptyState)
  it should "P! (!4) 2" in MergeableCase(S0, F0)(S1)(P1)(S1.rstate)
  it should "(P!) (!4)" in MergeableCase(S0, F_)(P_, S0)(Nil)(S0.rstate)
  it should "!P !4" in MergeableCase(F0)(P1)(S0, S1)(S1.rstate)
  // it should  "!P !4 2"   in ConflictingCase(F_)(P1)(S1)(emptyState)(S1.rstate) // TODO not clear
  it should "(!P) !4" in CoveredBy("(P!) !4")
  it should "!P (!4)" in MergeableCase(F0)(P1)(S0, S1)(S1.rstate)
  it should "(!P) (!4)" in CoveredBy("(P!) (!4)")
  it should "P! !C" in CurrentConflictMergeableCase(S0)(C1)(P0, S1)(S0.rstate ++ S1.rstate)(
    C1.rstate ++ P0.rstate
  )
  // it should  "P! (!C)" in NonDeterminedConflictCase(S0, C0)(S1)(P_)(emptyState)(emptyState)
  it should "(P!) !C" in ConflictingCase(C0)(P_, S1)(S0)(C0.rstate)(S0.rstate ++ S1.rstate)
  it should "(P!) (!C)" in ConflictingCase(S0, C_)(P_, S0)(Nil)(C_.rstate)(S0.rstate)
  it should "!P !C" in MergeableCase(P1)(C0)(S0, S1)(C0.rstate ++ S1.rstate)
  // it should  "!P !C 2"   in NonDeterminedConflictCase(P_)(C_)(S0, S1)(emptyState)(emptyState)
  it should "(!P) !C" in CoveredBy("(P!) !C")
  it should "!P (!C)" in CoveredBy("P! (!C)")
  it should "(!P) (!C)" in CoveredBy("(P!) (!C)")
  it should "P! 4X" in MergeableCase(S1)(Nil)(P_, F0)(S1.rstate ++ F0.rstate)
  it should "(P!) 4X" in ConflictingCase(P_, S0)(F_)(Nil)(S0.rstate)(F_.rstate)
  it should "!P 4X" in MergeableCase(P_)(F0)(S1)(S1.rstate ++ F0.rstate)
  it should "(!P) 4X" in CoveredBy("(P!) 4X")
  // it should  "P! 4!"     in NonDeterminedConflictCase(S1)(S0)(F_, P_)(emptyState)(emptyState)
  it should "P! 4!" in MergeableCase(S1)(S0)(P0, F1)(S0.rstate)
  it should "(P!) 4!" in MergeableCase(S0)(P1, S1)(F0)(S1.rstate)
  // it should  "(P!) 4! 2" in NonDeterminedConflictCase(S0)(P1, S1)(F_)(emptyState)(emptyState)
  it should "P! (4!)" in MergeableCase(S1)(F0, S0)(P1)(S1.rstate)
  // it should  "P! (4!) 2" in NonDeterminedConflictCase(S1)(F0, S0)(P_)(emptyState)(emptyState)
  it should "(P!) (4!)" in MergeableCase(F_, S0)(P_, S0)(Nil)(S0.rstate)
  it should "!P 4!" in MergeableCase(P_)(S0)(F0, S1)(S1.rstate)
  it should "(!P) 4!" in CoveredBy("(P!) 4!")
  it should "!P (4!)" in MergeableCase(P1)(F0, S0)(S1)(S1.rstate)
  it should "(!P) (4!)" in CoveredBy("(P!) (4!)")
  it should "P! 4!!" in MergeableCase(S0)(R1)(P0, F1)(S0.rstate ++ R1.rstate)
  // it should  "P! 4!!"   in NonDeterminedConflictCase(S0)(R1)(P0, F_)(S0.rstate ++ F_.rstate)(P0.rstate ++ R1.rstate)
  // it should  "(P!) 4!!" in NonDeterminedConflictCase(S1, P1)(R1)(F1)(emptyState)(emptyState)
  it should "(P!) 4!!" in MergeableCase(S0, P0)(R1)(F1)(S0.rstate ++ R1.rstate)
  it should "P! (4!!)" in MergeableCase(S0)(R1, F1)(P0)(S0.rstate ++ R1.rstate)
  it should "(P!) (4!!)" in MergeableCase(S1, P1)(R1, F1)(Nil)(S1.rstate ++ R1.rstate)
  it should "(P!) (4!!) 2" in MergeableCase(S1, P1)(R0, F0)(Nil)(S1.rstate ++ R0.rstate)
  it should "(P!) (4!!) 3" in MergeableCase(S1, P_)(R1, F_)(Nil)(S1.rstate ++ R1.rstate)
  it should "!P 4!!" in MergeableCase(P0)(R1)(S0, F1)(S0.rstate ++ R1.rstate)
  it should "!P 4!! 2" in MergeableCase(P_)(R1)(S0, F1)(S0.rstate ++ R1.rstate)
  it should "(!P) 4!!" in CoveredBy("(P!) 4!!")
  it should "!P (4!!)" in MergeableCase(P0)(R1, F1)(S0)(S0.rstate ++ R1.rstate)
  it should "(!P) (4!!)" in CoveredBy("(P!) (4!!)")
  it should "P! PX" in MergeableCase(S0)(Nil)(P1, P0)(S0.rstate ++ P1.rstate)
  it should "(P!) PX" in MergeableCase(S0, P_)(S0)(Nil)(S0.rstate ++ S0.rstate)
  it should "!P PX" in MergeableCase(P_)(Nil)(S1, P0)(S1.rstate ++ P0.rstate)
  it should "(!P) PX" in CoveredBy("(P!) PX")
  it should "P! P!" in MergeableCase(S0)(S1)(P0, P1)(S0.rstate ++ S1.rstate)
  it should "P! P! 2" in ConflictingCase(S1)(S0)(P_)(S1.rstate)(S0.rstate)
  // it should  "P! (P!)"    in NonDeterminedUnknownCase(S0)(P1, S1)(P_)(emptyState)(emptyState)
  it should "P! (P!) 2" in MergeableCase(S0)(P1, S1)(P0)(S0.rstate ++ S1.rstate)
  it should "(P!) P!" in CoveredBy("P! (P!)")
  it should "(P!) (P!)" in MergeableCase(P_, S0)(P_, S0)(Nil)(S0.rstate ++ S0.rstate)
  it should "P! !P" in MergeableCase(S0)(P1)(P0, S1)(S0.rstate ++ S1.rstate)
  it should "P! (!P)" in CoveredBy("P! (P!)")
  it should "(P!) !P" in MergeableCase(P_)(P0, S0)(S1)(S0.rstate ++ S1.rstate)
  it should "(P!) (!P)" in CoveredBy("(P!) (P!)")
  it should "!P !P" in MergeableCase(P0)(P1)(S0, S1)(S0.rstate ++ S1.rstate)
  it should "!P (!P)" in CoveredBy("P! (!P)")
  it should "(!P) !P" in CoveredBy("!P (!P)")
  it should "(!P) (!P)" in CoveredBy("(P!) (P!)")
  it should "P! P!!" in MergeableCase(S0)(R1)(P0, P1)(S0.rstate ++ R1.rstate)
  it should "(P!) P!!" in MergeableCase(S0, P_)(R1)(P1)(S0.rstate ++ R1.rstate)
  it should "P! (P!!)" in MergeableCase(S0)(R1, P1)(P0)(S0.rstate ++ R1.rstate)
  it should "P! (P!!) 2" in ConflictingCase(S0)(R1, S1)(P_)(S0.rstate)(R1.rstate ++ S1.rstate)
  it should "(P!) (P!!)" in MergeableCase(P_, S0)(R1, P_)(Nil)(S0.rstate ++ R1.rstate)
  it should "!P P!!" in MergeableCase(P1)(R0)(S1, P0)(S1.rstate ++ R0.rstate)
  it should "(!P) P!!" in CoveredBy("(P!) P!!")
  it should "!P (P!!)" in MergeableCase(P_)(P_, R0)(S1)(R0.rstate ++ S1.rstate)
  it should "(!P) (P!!)" in CoveredBy("(P!) (P!!)")
  it should "!P !!P" in MergeableCase(P0)(P1)(S0, R1)(S0.rstate ++ R1.rstate)
  it should "(!P) !!P" in MergeableCase(P_, S0)(P_)(R0)(S0.rstate ++ R0.rstate)
  it should "!P (!!P)" in CoveredBy("!P (P!!)")
  it should "(!P) (!!P)" in CoveredBy("(P!) (P!!)")
  it should "P! !!X" in MergeableCase(S1)(R0)(P0)(R0.rstate ++ S1.rstate)
  it should "(P!) !!X" in MergeableCase(S1, P_)(R0)(Nil)(S1.rstate ++ R0.rstate)
  it should "!P !!X" in MergeableCase(P_)(R1)(S0)(R1.rstate ++ S0.rstate)
  it should "(!P) !!X" in CoveredBy("(P!) !!X")
  it should "P! !!4" in MergeableCase(S0)(F_)(P0, R1)(R1.rstate ++ S0.rstate)
  it should "(P!) !!4" in MergeableCase(S0, P0)(F_)(R1)(R1.rstate ++ S0.rstate)
  it should "P! (!!4)" in MergeableCase(S0)(F_, R1)(P0)(R1.rstate ++ S0.rstate)
  it should "P! (!!4) 2" in ConflictingCase(S0)(F_, R1)(P_)(S0.rstate)(R1.rstate)
  it should "(P!) (!!4)" in CoveredBy("(P!) (4!!)")
  // it should  "!P !!4"     in NonDeterminedUnknownCase(P0)(F_)(S0, R1)(emptyState)(emptyState)
  it should "!P !!4 2" in MergeableCase(P0)(F1)(S0, R1)(S0.rstate ++ R1.rstate)
  it should "(!P) !!4" in CoveredBy("(!P) !!4")
  it should "!P (!!4)" in MergeableCase(P0)(F1, R1)(S0)(S0.rstate ++ R1.rstate)
  it should "(!P) (!!4)" in CoveredBy("(P!) (4!!)")
  // it should  "P! !!C"     in InfiniteLoop(S1)(C0)(P1, R0)(S1.rstate ++ R0.rstate)
  // it should  "(P!) !!C"   in InfiniteLoop(S1, P1)(C0)(R0)(S1.rstate ++ R0.rstate)
  // it should  "P! (!!C)"   in InfiniteLoop(S1)(C0, R0)(P1)(S1.rstate)
  // it should  "(P!) (!!C)" in InfiniteLoop(S1, P1)(C0, R0)(Nil)(S1.rstate)
  it should "P! CX" in CurrentConflictMergeableCase(S0)(C1)(P_)(S0.rstate)(C1.rstate ++ P_.rstate)
  it should "P! CX 2" in ConflictingCase(S0)(C_)(P_)(S0.rstate)(C_.rstate ++ P_.rstate)
  it should "(P!) CX" in ConflictingCase(C_)(P_, S0)(Nil)(C_.rstate)(S0.rstate)
  it should "!P CX" in MergeableCase(P_)(C1)(S0)(S0.rstate ++ C1.rstate)
  it should "(!P) CX" in CoveredBy("(P!) CX")
  // it should  "P! C!"      in NonDeterminedUnknownCase(S0)(S0)(P_, C_)(emptyState)(emptyState)
  it should "P! C! 2" in MergeableCase(S1)(S0)(P1, C0)(S1.rstate ++ C0.rstate)
  it should "P! (C!)" in CurrentConflictMergeableCase(S1)(C0, S0)(P1)(S1.rstate)(
    C0.rstate ++ P1.rstate
  )
  it should "(P!) C!" in MergeableCase(S1)(P0, S0)(C1)(S0.rstate ++ C1.rstate)
  // it should  "(P!) C! 2"  in NonDeterminedUnknownCase(S1)(P0, S0)(C_)(emptyState)(emptyState)
  it should "(P!) (C!)" in ConflictingCase(P_, S0)(C_, S0)(Nil)(S0.rstate)(C_.rstate)
  it should "!P C!" in MergeableCase(P_)(S1)(S0, C1)(S0.rstate ++ C1.rstate)
  it should "!P (C!)" in MergeableCase(P_)(C0, S0)(S1)(C0.rstate ++ S1.rstate)
  it should "(!P) C!" in CoveredBy("(P!) C!")
  it should "(!P) (C!)" in CoveredBy("(P!) (C!)")
  // it should  "P! C!!"     in InfiniteLoop(S1)(R0)(P1, C0)(S1.rstate ++ C0.rstate)
  // it should  "(P!) C!!"   in InfiniteLoop(S1, P1)(R0)(C0)(S1.rstate ++ C0.rstate)
  it should "P! (C!!)" in CoveredBy("P! (!!C)")
  it should "(P!) (C!!)" in CoveredBy("(P!) (!!C)")
  it should "P!! !X" in MergeableCase(Nil)(R0)(P0, S1)(R0.rstate ++ S1.rstate)
  it should "(P!!) !X " in MergeableCase(S0)(P_, R0)(Nil)(S0.rstate ++ R0.rstate)
  it should "!!P !X" in MergeableCase(Nil)(P0)(R0, S1)(R0.rstate ++ S1.rstate)
  it should "(!!P) !X" in CoveredBy("!!P !X")
  it should "P!! !4" in MergeableCase(F0)(R1)(S0, P1)(R1.rstate)
  // it should  "(P!!) !4"   in NonDeterminedUnknownCase(F1)(P_, R0)(S1)(emptyState)(emptyState)
  it should "P!! (!4)" in MergeableCase(S0, F0)(R1)(P1)(R1.rstate)
  it should "(P!!) (!4)" in MergeableCase(S0, F0)(P_, R0)(Nil)(R0.rstate)
  // it should  "!!P !4"     in NonDeterminedUnknownCase(F0)(P_)(S0, R1)(emptyState)(emptyState)
  it should "!!P !4 2" in MergeableCase(F0)(P1)(S0, R1)(R1.rstate)
  it should "(!!P) !4" in CoveredBy("(P!!) !4")
  it should "!!P (!4)" in MergeableCase(S0, F0)(P_)(R1)(R1.rstate)
  it should "(!!P) (!4)" in CoveredBy("(P!!) (!4)")
  it should "P!! !C" in MergeableCase(C0)(P1)(S0, R1)(C0.rstate ++ R1.rstate)
  it should "(P!!) !C" in CurrentConflictMergeableCase(C0)(R1, P1)(S0)(C0.rstate)(
    R1.rstate ++ S0.rstate
  )
  // it should  "(P!!) !C 2" in NonDeterminedUnknownCase(C_)(R1, P_)(S0)(emptyState)(emptyState)
  it should "P!! (!C)" in MergeableCase(S0, C0)(P_)(R1)(R1.rstate ++ C0.rstate)
  it should "(P!!) (!C)" in ConflictingCase(S0, C_)(P_, R0)(Nil)(C_.rstate)(R0.rstate)
  it should "!!P !C" in MergeableCase(C0)(P1)(S0, R1)(C0.rstate ++ R1.rstate)
  it should "(!!P) !C" in CoveredBy("(P!!) !C")
  it should "!!P (!C)" in MergeableCase(S0, C0)(P_)(R1)(R1.rstate ++ C0.rstate)
  it should "(!!P) (!C)" in CoveredBy("(P!!) (!C)")
  it should "P!! 4X" in CurrentConflictMergeableCase(F1)(R0)(P_)(F1.rstate ++ P_.rstate)(R0.rstate)
  it should "P!! 4X 2" in ConflictingCase(F_)(R0)(P_)(F_.rstate ++ P_.rstate)(R0.rstate)
  it should "(P!!) 4X" in ConflictingCase(F_)(P_, R0)(Nil)(F_.rstate)(R0.rstate)
  it should "!!P 4X" in MergeableCase(F_)(P_)(R0)(R0.rstate)
  it should "(!!P) 4X" in CoveredBy("(P!!) 4X")
  it should "P!! 4!" in MergeableCase(R1)(S0)(P1, F0)(R1.rstate)
  it should "(P!!) 4!" in MergeableCase(S0)(P1, R1)(F0)(R1.rstate)
  it should "P!! (4!)" in MergeableCase(F0, S0)(P_)(R1)(R1.rstate)
  it should "(P!!) (4!)" in MergeableCase(F_, S0)(P_, R0)(Nil)(R0.rstate)
  it should "!!P 4!" in MergeableCase(F0)(P1)(S0, R1)(R1.rstate)
  it should "(!!P) 4!" in CoveredBy("(P!!) 4!")
  // it should  "!!P (4!)"   in NonDeterminedMergeableCase(F_, S0)(P_)(R1)(R1.rstate)(R1.rstate)
  it should "(!!P) (4!)" in CoveredBy("(P!!) (4!)")
  it should "P!! 4!!" in MergeableCase(R0)(R1)(P0, F1)(R1.rstate ++ R0.rstate)
  it should "P!! 4!! 2" in ConflictingCase(R0)(R1)(P_, F_)(R0.rstate)(R1.rstate)
  it should "(P!!) 4!!" in MergeableCase(P0, R0)(R1)(F1)(R0.rstate ++ R1.rstate)
  it should "P!! (4!!)" in MergeableCase(R0)(R1, F1)(P0)(R0.rstate ++ R1.rstate)
  it should "(P!!) (4!!)" in MergeableCase(R0, P_)(R1, F_)(Nil)(R0.rstate ++ R1.rstate)
  it should "!!P 4!!" in MergeableCase(P_)(R0)(R1, F1)(R0.rstate ++ R1.rstate)
  it should "(!!P) 4!!" in CoveredBy("(P!!) 4!!")
  it should "!!P (4!!)" in MergeableCase(P_)(F_, R1)(R0)(R0.rstate ++ R1.rstate)
  it should "(!!P) (4!!)" in CoveredBy("(P!!) (4!!)")
  it should "P!! PX" in MergeableCase(R0)(S1)(P0)(S1.rstate ++ R0.rstate)
  it should "(P!!) PX" in CurrentConflictMergeableCase(P_, R0)(P1)(Nil)(R0.rstate)(P1.rstate)
  it should "(P!!) PX 2" in ConflictingCase(P_, R0)(P_)(Nil)(R0.rstate)(P_.rstate)
  it should "!!P PX" in MergeableCase(P_)(P1)(R0)(R0.rstate ++ P1.rstate)
  it should "P!! P!" in CoveredBy("P! P!!")
  it should "(P!!) P!" in CoveredBy("P! (P!!)")
  it should "P!! (P!)" in CoveredBy("(P!) P!!")
  it should "(P!!) (P!)" in CoveredBy("(P!) (P!!)")
  it should "!!P P!" in MergeableCase(S0)(P1)(P0, R1)(R1.rstate ++ S0.rstate)
  it should "(!!P) P!" in CoveredBy("(P!!) P!")
  // it should  "!!P (P!)"    in NonDeterminedMergeableCase(P_)(P_, S0)(R0)(R0.rstate)(R0.rstate)
  it should "(!!P) (P!)" in CoveredBy("(P!!) (P!)")
  it should "P!! P!!" in MergeableCase(R1)(R0)(P1, P0)(R0.rstate ++ R1.rstate)
  it should "(P!!) P!!" in MergeableCase(R0)(P_, R1)(P0)(R0.rstate ++ R1.rstate)
  it should "(P!!) (P!!)" in MergeableCase(P_, R0)(P_, R0)(Nil)(R0.rstate ++ R0.rstate)
  it should "!!P P!!" in MergeableCase(R1)(P0)(R0, P1)(R0.rstate ++ R1.rstate)
  it should "(!!P) P!!" in CoveredBy("(P!!) P!!")
  it should "(!!P) (P!!)" in CoveredBy("(P!!) (P!!)")
  it should "P!! !!X" in MergeableCase(R1)(R0)(P1)(R0.rstate ++ R1.rstate)
  it should "(P!!) !!X" in MergeableCase(R1, P1)(R0)(Nil)(R0.rstate ++ R1.rstate)
  it should "!!P !!X" in MergeableCase(P_)(Nil)(R1, R0)(R0.rstate ++ R1.rstate)
  it should "P!! !!4" in MergeableCase(R0)(F1)(P0, R1)(R0.rstate ++ R1.rstate)
  it should "P!! !!4 2" in MergeableCase(R0)(F_)(P0, R1)(R0.rstate ++ R1.rstate)
  it should "(P!!) !!4" in MergeableCase(R0, P_)(F_)(R1)(R0.rstate ++ R1.rstate)
  it should "P!! (!!4)" in CoveredBy("P!! (4!!)")
  it should "(P!!) (!!4)" in CoveredBy("(P!!) (4!!)")
  it should "!!P !!4" in MergeableCase(P_)(F_)(R0)(R0.rstate)
  it should "(!!P) !!4" in CoveredBy("(P!!) !!4")
  it should "!!P (!!4)" in CoveredBy("!!P (4!!)")
  // it should  "P!! !!C"     in InfiniteLoop(R0)(C1)(P0, R1)(R0.rstate ++ R1.rstate)
  // it should  "(P!!) !!C"   in InfiniteLoop(R0, P0)(C1)(R1)(R0.rstate ++ R1.rstate)
  // it should  "P!! (!!C)"   in InfiniteLoop(R0)(C1, R1)(P0)(R0.rstate)
  // it should  "(P!!) (!!C)" in InfiniteLoop(R0, P0)(C1, R1)(Nil)(R0.rstate)
  it should "P!! CX" in CurrentConflictMergeableCase(R0)(C1)(P_)(R0.rstate)(C1.rstate ++ P_.rstate)
  it should "P!! CX 2" in ConflictingCase(R0)(C_)(P_)(R0.rstate)(C_.rstate ++ P_.rstate)
  it should "(P!!) CX" in ConflictingCase(C_)(P_, R0)(Nil)(C_.rstate)(R0.rstate)
  it should "!!P CX" in MergeableCase(P_)(C1)(R0)(R0.rstate ++ C1.rstate)
  it should "(!!P) CX" in CoveredBy("(P!!) CX")
  it should "P!! C!" in MergeableCase(S1)(R0)(C1, P0)(R0.rstate ++ C1.rstate)
  it should "(P!!) C!" in MergeableCase(S1)(P_, R0)(C1)(R0.rstate ++ C1.rstate)
  // it should  "P!! (C!)"     in NonDeterminedConflictCase(R0)(C_, S0)(P_)(R0.rstate)(C_.rstate)
  // it should  "P!! (C!) 2"   in NonDeterminedConflictCase(R0)(C1, S1)(P_)(R0.rstate)(C1.rstate)
  it should "(P!!) (C!)" in ConflictingCase(P_, R0)(C_, S0)(Nil)(R0.rstate)(C_.rstate)
  it should "(P!!) (C!) 2" in CurrentConflictMergeableCase(P1, R1)(C0, S0)(Nil)(R1.rstate)(
    C0.rstate
  )
  it should "!!P C!" in MergeableCase(P0)(S1)(R0, C1)(R0.rstate ++ C1.rstate)
  it should "(!!P) C!" in CoveredBy("(P!!) C!")
  it should "!!P (C!)" in CoveredBy("!!P (!C)")
  it should "(!!P) (C!)" in CoveredBy("(P!!) (C!)")
  // it should  "P!! C!!"      in InfiniteLoop(R1)(R0)(P1, C0)(R1.rstate ++ C0.rstate)
  // it should  "(P!!) C!!"    in InfiniteLoop(R1, P1)(R0)(C0)(R1.rstate ++ C0.rstate)
  it should "P!! (C!!)" in CoveredBy("P!! (!!C)")
  it should "(P!!) (C!!)" in CoveredBy("(P!!) (C!!)")
}
