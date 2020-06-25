package actors


import akka.actor.{Actor, ActorLogging, Props}
import api.{NetBalanceMonthly, NotOkResponse, TransactionProcessed}
import domain._
import messages._
import org.joda.time.DateTime

object AccountHistoryManager {
  def props(accountNumber: String) = Props(new AccountHistoryManager(accountNumber))
}

class AccountHistoryManager(val accountNumber: String) extends Actor with ActorLogging {

  var history = AccountHistory(accountNumber, Vector.empty[Transaction])
  var currentBalance: Double = 0.0

  override def receive: Receive = {

    case ProcessTransaction( _ , trx) => {
      processTransaction(trx)
      log.info("Transaction processed")
      sender() ! TransactionProcessed(trx.transactionId)
    }

    case GetTransactionsByMonth(_) => {
      if (history.transactions.nonEmpty) {
        val netTransactionsPerMonth = history.transactions.foldLeft(Map[Int, Map[Int, Double]]()){
          (acc: Map[Int, Map[Int, Double]], trx: Transaction) =>
            val year = new DateTime(trx.time).getYear
            val month = new DateTime(trx.time).monthOfYear().get()

            val monthMap: Map[Int, Double] = acc.getOrElse(year, Map())

            acc.updated(year, monthMap.updated(month, monthMap.getOrElse(month, 0.0) + trx.amount))
        }
        log.info(s"EndOfMonthBalances computed for account $accountNumber")
        val wrapped = netTransactionsPerMonth.map{ case (key, value: Map[Int, Double]) => (key.toString, value.map {case (k, v) => (k.toString, v)})}
        sender() ! NetBalanceMonthly(wrapped)
      } else sender() ! NotOkResponse(NOK("No transactions for account"))
    }
  }


  override def postStop(): Unit = {
    log.debug(s"Actor of $accountNumber died with state $currentBalance")
  }

  private[actors] def processTransaction(transaction: Transaction): Unit = {
    this.history = history addTransaction transaction
    this.currentBalance += transaction.amount
  }
}
