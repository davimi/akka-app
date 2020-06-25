package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import api.{NetBalanceMonthly, TransactionProcessed}
import domain.Transaction
import messages._
import org.joda.time.DateTime
import org.scalatest.{Matchers, OneInstancePerTest, WordSpecLike}

import scala.collection.immutable.Map

class AccountHistoryManagerSpec
  extends TestKit(ActorSystem("AccountHistoryManagerSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with OneInstancePerTest
  with StopAkkaSystem {

  def randomAccountNr() = util.Random.nextInt().abs.toString

  "The AccountHistoryManager" should {
    "build internal state" should {
      "process a transaction" in {
        val accountNr = randomAccountNr()
        val accountHistoryManager: TestActorRef[AccountHistoryManager] = TestActorRef(new AccountHistoryManager(accountNr))
        val trxAmount = 10.0
        val trx = Transaction("abc", trxAmount, accountNr, DateTime.now().getMillis)
        accountHistoryManager ! ProcessTransaction(accountNr, trx)
        accountHistoryManager.underlyingActor.history.transactions.length should equal(1)
        accountHistoryManager.underlyingActor.currentBalance should equal(trxAmount)
      }

      "process two transactions and compute correct balance" in {
        val accountNr = randomAccountNr()
        val accountHistoryManager: TestActorRef[AccountHistoryManager] = TestActorRef(new AccountHistoryManager(accountNr))
        val trxAmount = 10.0
        val trx2Amount = -80.23
        val trx = Transaction("abc", trxAmount, accountNr, DateTime.now().getMillis)
        val trx2 = Transaction("abcd", trx2Amount, accountNr, DateTime.now().getMillis)
        accountHistoryManager ! ProcessTransaction(accountNr, trx)
        accountHistoryManager ! ProcessTransaction(accountNr, trx2)
        accountHistoryManager.underlyingActor.history.transactions.length should equal(2)
        accountHistoryManager.underlyingActor.currentBalance should equal(trxAmount + trx2Amount)
      }
    }

    "do messaging" should {
      "send an GetEndOfMonthBalances" in {
        val accountNr = randomAccountNr()
        val accountHistoryManager = system.actorOf(AccountHistoryManager.props(accountNr))
        val trxAmount = 10.0
        val trx2Amount = -15.0
        val now = DateTime.now()
        val trx = Transaction("abc", trxAmount, accountNr, now.getMillis)
        val trx2 = Transaction("abcd", trx2Amount, accountNr, now.getMillis)
        accountHistoryManager ! ProcessTransaction(accountNr, trx)
        expectMsg(TransactionProcessed(trx.transactionId))
        accountHistoryManager ! ProcessTransaction(accountNr, trx2)
        expectMsg(TransactionProcessed(trx2.transactionId))
        accountHistoryManager ! GetTransactionsByMonth(accountNr)
        expectMsg(NetBalanceMonthly(Map(now.getYear.toString -> Map(now.monthOfYear().get.toString -> -5.0))))
      }
    }
  }
}
