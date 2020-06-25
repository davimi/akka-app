package api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import domain.Transaction
import serialization.AccountInsightsJsonParser
import AccountInsightsJsonParser._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, ResponseEntity, StatusCodes}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Interface for the routing
  */
trait AccountInsightsRestApi {


  def getNetBalancePerMonth: String => Future[ApiResponse]
  def createAccount: String => Future[ApiResponse]
  def processTransaction: Transaction => Future[ApiResponse]
  def createDemoState: String => Future[ApiResponse]

  val logger = LoggerFactory.getLogger(classOf[AccountInsightsRestApi])

  //TODO: Make routes more RESTish (no verbs)
  def route: Route =
    accountNetBalancePerMonthRoute(getNetBalancePerMonth) ~
    createRoute(createAccount) ~
    processTransactionRoute(processTransaction) ~
    createDemoStateRoute(createDemoState)

  def accountNetBalancePerMonthRoute(getNetAccountBalanceMonthly: String => Future[ApiResponse]): Route = {

    pathPrefix("accounts" / Segment / "transactiontotal" / "monthly") { account =>
      pathEndOrSingleSlash {
        get {
          logger.debug("Got request to get balance of account: {}", account)
          onSuccess(getNetAccountBalanceMonthly(account)) {
            case nbm: NetBalanceMonthly => complete(nbm)
            case _: NotOkResponse => complete(HttpResponse(status = StatusCodes.Forbidden))
          }
        }
      }
    }
  }

  def createRoute(createAccount: String => Future[ApiResponse]): Route = {
    pathPrefix("accounts" / "create" ) {
      pathEndOrSingleSlash {
        post {
          entity(as[String]) { account =>
            logger.debug("Got request to create account: {}", account)
            onSuccess(createAccount(account)) {
              case accCrtd: AccountCreated => complete(accCrtd)
              case _: NotOkResponse => complete(HttpResponse(status = StatusCodes.Conflict, entity = HttpEntity("Account already exists.\n")))
            }
          }
        }
      }
    }
  }

  def processTransactionRoute(processTransaction: Transaction => Future[ApiResponse]): Route = {
    pathPrefix("accounts" / "processtransaction" ) {
      pathEndOrSingleSlash {
        post {
          entity(as[Transaction]) { transaction: Transaction =>
            logger.debug("Got request to process transaction: {}", transaction)
            onSuccess(processTransaction(transaction)) {
              case tp: TransactionProcessed => complete(tp)
              case _: NotOkResponse => complete(StatusCodes.BadRequest)
            }
          }
        }
      }
    }
  }

  def createDemoStateRoute(createDemoState: String => Future[ApiResponse]): Route = {
    pathPrefix("accounts" / "demo" / Segment) { accountNr =>
      pathEndOrSingleSlash {
        post {
          logger.info("Creating demo state for account number: {}", accountNr)
          onSuccess(createDemoState(accountNr)) {
            case _ => complete(StatusCodes.OK)
          }
        }
      }
    }
  }

}
