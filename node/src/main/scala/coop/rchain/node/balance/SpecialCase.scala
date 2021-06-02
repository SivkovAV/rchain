package coop.rchain.node.balance

import cats.effect.Sync
import cats.implicits._
import coop.rchain.casper.protocol.BlockMessage
import coop.rchain.node.balance.TransactionBalances.{GlobalVaultsInfo, RevAccount, Transfer}
import coop.rchain.rholang.interpreter.RhoRuntime
import coop.rchain.shared.Log
import coop.rchain.casper.syntax._
import coop.rchain.metrics.Span

/**
  * Hard-coding the special cases in RChain Mainnet. Currently what the transaction server is missing is the
  * transfers in CloseBlock system deploy and these transfers only happened in slashing block and epoch block which
  * are all we need for special cases.
  *
  * 1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5 is the POS vault address.
  */
object SpecialCase {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def getSpecialTransfer(blockNumber: Long) =
    if (blockNumber > 0 && blockNumber < 166708L) {
      Vector.empty[Transfer]
    } else if (blockNumber < 250000L && blockNumber >= 166708L) {
      slashAt166708
    } else if (blockNumber >= 250000L && blockNumber < 464403L) {
      slashAt166708 ++ epochAt250000
    } else if (blockNumber >= 464403L && blockNumber < 500000L) {
      slashAt166708 ++ epochAt250000 ++ slashAt463304
    } else if (blockNumber >= 500000L && blockNumber < 750000L) {
      slashAt166708 ++ epochAt250000 ++ slashAt463304 ++ epochAt500000
    } else if (blockNumber >= 750000L) {
      slashAt166708 ++ epochAt250000 ++ slashAt463304 ++ epochAt500000 ++ epochAt750000
    } else {
      throw new Exception(s"Impossible target blockNumber ${blockNumber}.")
    }

  val slashAt166708 = Vector(
    // Rewards Transfer
    Transfer(
      201095627L,
      "11112cX59qJxUURdFgQepSuNC2CpV5AZ6FsrtMZpTbXUNFjhQiNRZc",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      4110853L,
      "11112Rb5XSQJeuVeTuP8ieSQqucS7rUUxio2SrGyCa7Bj6qaiEbnB7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      132558557L,
      "11112SM2Fi38J9fdgCvujzg4DZaYrQYSHSt9cZPwsMQiB6jp2fU7Cd",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      14578288L,
      "1111iHcepxeET3XqwR3a177ZyEbVTN55pDypGdDSnvf4dWvZ4Ws3m",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      61194583L,
      "11112RsUiYwzGsM4C1w1KXzqLZUK4WcEWiZueviiJhWriHkSmNpTza",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      373856758L,
      "1111oYt5g3EKfuizbJUrFunhxdmowFQUXzrDki2qFQ8uunFmQwqSm",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      27137423L,
      "11112eeX28rcEGzZ9CjZAeyanvQAop7n9siE8zSep4BiLG6NMvhs9Z",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      5331022L,
      "11112en8X2AryCGtakTQvokzpEurgWfAH6CzjHPg3UTWbrBvQjkR9g",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      678960L,
      "1111gLbVCFKvgdHx3WXfCsPPu9rc6QK982Di8Ky2mrCm3KnrAF4Kw",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    Transfer(
      7078909L,
      "1111LowRZnbRzU3uGZ5GVMAcZigLSWsxqaRydyujkK8drU2iggUbf",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      166708L
    ),
    // slash transfer
    Transfer(
      81703406075708L,
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      "11112q61nMYJKnJhQmqz7xKBNupyosG4Cy9rVupBPmpwcyT6s2SAoF",
      166708L
    )
  )

  val epochAt250000 = Vector(
    Transfer(
      161505260L,
      "11112cX59qJxUURdFgQepSuNC2CpV5AZ6FsrtMZpTbXUNFjhQiNRZc",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      7473548L,
      "11112Rb5XSQJeuVeTuP8ieSQqucS7rUUxio2SrGyCa7Bj6qaiEbnB7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      10507324L,
      "11112SM2Fi38J9fdgCvujzg4DZaYrQYSHSt9cZPwsMQiB6jp2fU7Cd",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      37922815L,
      "1111iHcepxeET3XqwR3a177ZyEbVTN55pDypGdDSnvf4dWvZ4Ws3m",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      3000000108853683L,
      "11112RsUiYwzGsM4C1w1KXzqLZUK4WcEWiZueviiJhWriHkSmNpTza",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      136932995L,
      "1111oYt5g3EKfuizbJUrFunhxdmowFQUXzrDki2qFQ8uunFmQwqSm",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      63739407L,
      "11112eeX28rcEGzZ9CjZAeyanvQAop7n9siE8zSep4BiLG6NMvhs9Z",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      115040341L,
      "11112en8X2AryCGtakTQvokzpEurgWfAH6CzjHPg3UTWbrBvQjkR9g",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    ),
    Transfer(
      17190789L,
      "1111LowRZnbRzU3uGZ5GVMAcZigLSWsxqaRydyujkK8drU2iggUbf",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      250000L
    )
  )

  val slashAt463304 = Vector(
    // Rewards Transfer
    Transfer(
      20996717L,
      "11112cX59qJxUURdFgQepSuNC2CpV5AZ6FsrtMZpTbXUNFjhQiNRZc",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      6967993L,
      "11112Rb5XSQJeuVeTuP8ieSQqucS7rUUxio2SrGyCa7Bj6qaiEbnB7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      5436743L,
      "11112SM2Fi38J9fdgCvujzg4DZaYrQYSHSt9cZPwsMQiB6jp2fU7Cd",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      17655855L,
      "1111iHcepxeET3XqwR3a177ZyEbVTN55pDypGdDSnvf4dWvZ4Ws3m",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      14285184L,
      "11112RsUiYwzGsM4C1w1KXzqLZUK4WcEWiZueviiJhWriHkSmNpTza",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      2336393L,
      "1111oYt5g3EKfuizbJUrFunhxdmowFQUXzrDki2qFQ8uunFmQwqSm",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      3396783L,
      "11112eeX28rcEGzZ9CjZAeyanvQAop7n9siE8zSep4BiLG6NMvhs9Z",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      3078862L,
      "11112en8X2AryCGtakTQvokzpEurgWfAH6CzjHPg3UTWbrBvQjkR9g",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      232617614L,
      "1111gLbVCFKvgdHx3WXfCsPPu9rc6QK982Di8Ky2mrCm3KnrAF4Kw",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      4026077L,
      "1111LowRZnbRzU3uGZ5GVMAcZigLSWsxqaRydyujkK8drU2iggUbf",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      2733199L,
      "1111GUeqjdNVp7oDDwuo8kUMRjiCZ4qWkatMGsgvffyCrJxVZZikL",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      5825090L,
      "11112QkNjD95s6i3MstxsYcPHC4qx5QGVTrXcdbSAB4XmqoL2N3VFW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      5276355L,
      "11112MW6m6rbew1imHqcLLRsbEg8M1tRZBXcF8vGJABjjbYo3iPcC5",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      11645779L,
      "1111e5UJH7AwRQRDxBhi5nRmjg6nRzhN99WTBKaRerW5h6s8X3mqN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      19772171L,
      "1111PTguwCwfLDKfa51oZJqhs1szwcuenApEkvykJp3Q4Y6d79ypJ",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      89007421L,
      "11113KWXYsxJivtMKGf3LW2js9GfK3e5vdL59aerHqm1Bc9t1juKr",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      6342232L,
      "11119LqBoAQP7zV3LXXbjRL7kaR1DLWSfn2xGsvPxTw6zh5weNFBN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      27397907L,
      "1111TbBXzp2atjrpSpD641i5R1MZdGHqPaMT7UFibASvaNWvpdGEK",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      3345403L,
      "111122HCfMu4STPqspYmEwrh6VpeHW6PtB23iuh9EzvzzAw63mEAy7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    ),
    Transfer(
      3307019L,
      "1111YHTd2CNaSEKBrAU4vjeVFZTzew4LTJ2YgGcQJkPNfnRhrvhUW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      463304L
    )
    // slash transfer. This slash transfer failed because we move all the pos vault money out as bug and not enough
    // fund insider.
//  Transfer(150000003509482L,"1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5","11112q61nMYJKnJhQmqz7xKBNupyosG4Cy9rVupBPmpwcyT6s2SAoF",463304L)
  )

  val epochAt500000 = Vector(
    Transfer(
      3095764L,
      "11112cX59qJxUURdFgQepSuNC2CpV5AZ6FsrtMZpTbXUNFjhQiNRZc",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1196466L,
      "11112Rb5XSQJeuVeTuP8ieSQqucS7rUUxio2SrGyCa7Bj6qaiEbnB7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      576950L,
      "11112SM2Fi38J9fdgCvujzg4DZaYrQYSHSt9cZPwsMQiB6jp2fU7Cd",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      150000001946223L,
      "11112RsUiYwzGsM4C1w1KXzqLZUK4WcEWiZueviiJhWriHkSmNpTza",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      484657L,
      "1111oYt5g3EKfuizbJUrFunhxdmowFQUXzrDki2qFQ8uunFmQwqSm",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1395092L,
      "11112eeX28rcEGzZ9CjZAeyanvQAop7n9siE8zSep4BiLG6NMvhs9Z",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      742582L,
      "11112en8X2AryCGtakTQvokzpEurgWfAH6CzjHPg3UTWbrBvQjkR9g",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      23075025L,
      "1111gLbVCFKvgdHx3WXfCsPPu9rc6QK982Di8Ky2mrCm3KnrAF4Kw",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      249116L,
      "1111LowRZnbRzU3uGZ5GVMAcZigLSWsxqaRydyujkK8drU2iggUbf",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1045654L,
      "1111GUeqjdNVp7oDDwuo8kUMRjiCZ4qWkatMGsgvffyCrJxVZZikL",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1051841L,
      "11112QkNjD95s6i3MstxsYcPHC4qx5QGVTrXcdbSAB4XmqoL2N3VFW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1644461L,
      "11112MW6m6rbew1imHqcLLRsbEg8M1tRZBXcF8vGJABjjbYo3iPcC5",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1861940L,
      "1111e5UJH7AwRQRDxBhi5nRmjg6nRzhN99WTBKaRerW5h6s8X3mqN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1376417L,
      "1111PTguwCwfLDKfa51oZJqhs1szwcuenApEkvykJp3Q4Y6d79ypJ",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      7850911L,
      "11113KWXYsxJivtMKGf3LW2js9GfK3e5vdL59aerHqm1Bc9t1juKr",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      485313L,
      "11119LqBoAQP7zV3LXXbjRL7kaR1DLWSfn2xGsvPxTw6zh5weNFBN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1500000004318729L,
      "1111TbBXzp2atjrpSpD641i5R1MZdGHqPaMT7UFibASvaNWvpdGEK",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      1137270L,
      "111122HCfMu4STPqspYmEwrh6VpeHW6PtB23iuh9EzvzzAw63mEAy7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    ),
    Transfer(
      904005L,
      "1111YHTd2CNaSEKBrAU4vjeVFZTzew4LTJ2YgGcQJkPNfnRhrvhUW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      500000L
    )
  )

  val epochAt750000 = Vector(
    Transfer(
      36633144L,
      "11112cX59qJxUURdFgQepSuNC2CpV5AZ6FsrtMZpTbXUNFjhQiNRZc",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      21598280L,
      "11112Rb5XSQJeuVeTuP8ieSQqucS7rUUxio2SrGyCa7Bj6qaiEbnB7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      21620353L,
      "11112SM2Fi38J9fdgCvujzg4DZaYrQYSHSt9cZPwsMQiB6jp2fU7Cd",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      29980136L,
      "1111iHcepxeET3XqwR3a177ZyEbVTN55pDypGdDSnvf4dWvZ4Ws3m",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      18243710L,
      "11112RsUiYwzGsM4C1w1KXzqLZUK4WcEWiZueviiJhWriHkSmNpTza",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      31101727L,
      "1111oYt5g3EKfuizbJUrFunhxdmowFQUXzrDki2qFQ8uunFmQwqSm",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      12834281L,
      "11112eeX28rcEGzZ9CjZAeyanvQAop7n9siE8zSep4BiLG6NMvhs9Z",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      31809550L,
      "11112en8X2AryCGtakTQvokzpEurgWfAH6CzjHPg3UTWbrBvQjkR9g",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      33223645L,
      "1111gLbVCFKvgdHx3WXfCsPPu9rc6QK982Di8Ky2mrCm3KnrAF4Kw",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      31957445L,
      "1111LowRZnbRzU3uGZ5GVMAcZigLSWsxqaRydyujkK8drU2iggUbf",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      27621277L,
      "1111GUeqjdNVp7oDDwuo8kUMRjiCZ4qWkatMGsgvffyCrJxVZZikL",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      17549501L,
      "11112QkNjD95s6i3MstxsYcPHC4qx5QGVTrXcdbSAB4XmqoL2N3VFW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      635954279L,
      "11112MW6m6rbew1imHqcLLRsbEg8M1tRZBXcF8vGJABjjbYo3iPcC5",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      17104722L,
      "1111e5UJH7AwRQRDxBhi5nRmjg6nRzhN99WTBKaRerW5h6s8X3mqN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      34661492L,
      "1111PTguwCwfLDKfa51oZJqhs1szwcuenApEkvykJp3Q4Y6d79ypJ",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      22539807L,
      "11113KWXYsxJivtMKGf3LW2js9GfK3e5vdL59aerHqm1Bc9t1juKr",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      26129080L,
      "11119LqBoAQP7zV3LXXbjRL7kaR1DLWSfn2xGsvPxTw6zh5weNFBN",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      25091746L,
      "1111TbBXzp2atjrpSpD641i5R1MZdGHqPaMT7UFibASvaNWvpdGEK",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      32740009L,
      "111122HCfMu4STPqspYmEwrh6VpeHW6PtB23iuh9EzvzzAw63mEAy7",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      25648787L,
      "1111YHTd2CNaSEKBrAU4vjeVFZTzew4LTJ2YgGcQJkPNfnRhrvhUW",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      24064488L,
      "1111uY3tSgMz9cj8a6Y4Gr8TrrYyhD3zu8BFRH7qg2wGPkafopknb",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      28436645L,
      "11112kmj2qDxWLUDMfpZf1MAa62m7hoiUTXnrexyqvZC7Dpi1V9ZtU",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      27385835L,
      "11112SWxWtZUDQ1SntUoGVPQeSTzVuWcoyP2P7R7vJgmaz1uVaGxi1",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      19716897L,
      "1111CyFrj85AwWtZzenh6Q29C1q8U3UmcZ4pvADrSY89bt4VFEjnj",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      44751944L,
      "1111yjVxMb3Dca3L8HyH7QDPZL2EKeBDG7PWgMHvBFmTj2WAQDF6B",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      16227724L,
      "1111APUCMaMS8LryGEdtGLSWaQ18VCg81ehyAx1kjFbfgiZpL8v6k",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      14459871L,
      "11112otedAe9yYeeLJoxc4gKBifKdFj4SjxxmoaFjoPFYLc9U8NU1f",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      19873568L,
      "1111244BKRXnje2hXXZaVZGVFmMys9n6F2qN24VNRtwSEdLkmnaU5k",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      23502807L,
      "1111k2zMZbqcV4yNua6YQgUiCxRBubogkFzK6UrSKNCGXRKoGSAK3",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    ),
    Transfer(
      20670311L,
      "11112UyJvNyopZ9BPktzskA7fV4mZLCvv4k3jW22cssHDGdyuN21zH",
      "1111gW5kkGxHg7xDg6dRkZx2f7qxTizJzaCH9VEM1oJKWRvSX9Sk5",
      750000L
    )
  )
}
