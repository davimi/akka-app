package actors

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import api.{AccountCreated, NotOkResponse, TransactionProcessed}
import domain.Transaction
import messages._
import org.joda.time.DateTime
import org.scalatest.{Matchers, OneInstancePerTest, WordSpecLike}

class AccountHistoriesSupervisorSpec
  extends TestKit(ActorSystem("AccountHistorySupervisorSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with OneInstancePerTest
  with StopAkkaSystem {


  "The AccountHistorySupervisor" should {

    def randomAccountNr() = util.Random.nextInt().abs.toString

    "create accounts" should {
      "create a account history" in {
        val accountHistorySupervisor = TestActorRef[AccountHistoriesSupervisor]
        accountHistorySupervisor ! CreateAccountManager(randomAccountNr())
        accountHistorySupervisor.children.size shouldEqual 1
      }

      "send OK for created account histories" in {
        val accountNr = randomAccountNr()
        val accountHistorySupervisor = system.actorOf(Props[AccountHistoriesSupervisor])
        accountHistorySupervisor ! CreateAccountManager(accountNr)
        expectMsg(AccountCreated(accountNr))
      }

      "send NOK for already account histories" in {
        val accountNr = randomAccountNr()
        val accountHistorySupervisor = system.actorOf(Props[AccountHistoriesSupervisor])
        accountHistorySupervisor ! CreateAccountManager(accountNr)
        expectMsg(AccountCreated(accountNr))

        accountHistorySupervisor ! CreateAccountManager(accountNr)
        expectMsg(NotOkResponse(NOK("Account exists already")))
      }
    }

    "get end of month balances" should {
      "reject requests" in {
        val accountNr = randomAccountNr()
        val accountHistorySupervisor = system.actorOf(Props[AccountHistoriesSupervisor])
        accountHistorySupervisor ! GetTransactionsByMonth(accountNr)
        expectMsg(NotOkResponse(NOK("No account exists to get balance")))
      }
    }

    "forward transactions" should {
      "send transactions to the child" in {
        val accountNr = randomAccountNr()
        val accountHistorySupervisor = system.actorOf(Props[AccountHistoriesSupervisor])
        accountHistorySupervisor ! CreateAccountManager(accountNr)
        expectMsg(AccountCreated(accountNr))

        val trx = Transaction("abcd", 10.0, accountNr, DateTime.now.getMillis)
        accountHistorySupervisor ! ProcessTransaction(accountNr, trx)
        expectMsg(TransactionProcessed(trx.transactionId))
      }

      "complain if child does not exist" in {
        val accountNr = randomAccountNr()
        val accountHistorySupervisor = system.actorOf(AccountHistoriesSupervisor.props)
        val trx = Transaction("abcd", 10.0, accountNr, DateTime.now.getMillis)
        accountHistorySupervisor ! ProcessTransaction(accountNr, trx)
        expectMsg(NotOkResponse(NOK("No account exists for transaction")))
      }
    }
  }
}
