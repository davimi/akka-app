package service

import actors.AccountHistoriesSupervisor
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import api.ApiResponse
import domain.Transaction
import messages.{CreateAccountManager, GetTransactionsByMonth, ProcessTransaction}
import util.Random
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Interface of the business logic
  */
trait AccountInsightsApi {

  implicit def actorSystem: ActorSystem
  implicit def executionContext: ExecutionContext = actorSystem.dispatcher

  implicit def requestTimeout: Timeout

  lazy val backend: ActorRef = createBackend() //lazy in order to avoid it being called before initialized in implementing class

  /**
    * Default implementation of creating the backend. Override to provide different backend.
    */
  def createBackend(): ActorRef = actorSystem.actorOf(AccountHistoriesSupervisor.props)

  def getNetBalancePerMonth: String => Future[ApiResponse] = { accountNr =>
    backend.ask(GetTransactionsByMonth(accountNr)).mapTo[ApiResponse]
  }

  def createAccount: String => Future[ApiResponse] = { accountNr: String =>
    backend.ask(CreateAccountManager(accountNr)).mapTo[ApiResponse]
  }

  def processTransaction: Transaction => Future[ApiResponse] = { transaction: Transaction =>
    backend.ask(ProcessTransaction(transaction.accountNumber, transaction)).mapTo[ApiResponse]
  }

  def createDemoState: String => Future[ApiResponse] = { accountNr: String =>
    val currentYear = DateTime.now().year().get()
    backend.ask(CreateAccountManager(accountNr))
      .andThen { case _ => backend.ask(ProcessTransaction(accountNr, Transaction(Random.nextString(10), 2000.0, accountNr, new DateTime(currentYear, 1, 1, 12, 0).getMillis)))}
      .andThen { case _ => (1 to 12).map(m => backend.ask(ProcessTransaction(accountNr, createRandomTransaction(accountNr, currentYear, m))))}
      .mapTo[ApiResponse]
  }

  private def createRandomTransaction(accountNr: String, year: Int, month: Int): Transaction = {
    Transaction(Random.nextString(10), Random.nextGaussian() * 100 + 50, accountNr, new DateTime(year, month, 25, 12, 0).getMillis)
  }

}
