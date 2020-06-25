package api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.Transaction
import messages.NOK
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import serialization.AccountInsightsJsonParser._

import scala.concurrent.Future


class AccountInsightRestApiSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val data = Map("2018" -> Map("1" -> 0.0, "2" -> 500.0))
  val defaultAccountNr = "NLINGB666"
  val transactionId = "trx123"

  class AccountInsightRestApiFixture extends AccountInsightsRestApi { //TODO: Should be properly mocked
    var defaultAccountCreated = false
    override def getNetBalancePerMonth: String => Future[NetBalanceMonthly] = {_: String => Future.successful(NetBalanceMonthly(data))}
    override def createAccount: String => Future[AccountCreated] = { _ => Future.successful(AccountCreated(defaultAccountNr))}
    override def processTransaction: Transaction => Future[TransactionProcessed] = { trx: Transaction => Future.successful(TransactionProcessed(trx.transactionId))}
    override def createDemoState: String => Future[ApiResponse] = { _ => Future.successful(AccountCreated(defaultAccountNr))}
  }

  val api = new AccountInsightRestApiFixture

  "The REST API" should {
    "respond to a get request of net balances monthly" in {
      Get("/accounts/123/transactiontotal/monthly") ~>  api.route ~> check {

        responseAs[NetBalanceMonthly].data("2018")("1") should equal(0.0)
      }
    }

    "respond to a post request for new accounts" in {
      Post("/accounts/create/", defaultAccountNr) ~>  api.route ~> check {
        responseAs[AccountCreated].accountNr should equal(defaultAccountNr)
      }
    }

    "respond to a post for processing transactions" in {
      val transactionJson = """{"transactionId":"1234","amount":300.5,"accountNumber":"NL1234","time":1526724383307}"""
      Post("/accounts/processtransaction/", HttpEntity(ContentTypes.`application/json`, transactionJson)) ~>  api.route ~> check {
        responseAs[TransactionProcessed].transactionId should equal("1234")
      }
    }

    "respond to a post for creating demo state" in {
      Post(s"/accounts/demo/$defaultAccountNr") ~>  api.route ~> check {
        response.status equals StatusCodes.OK
      }
    }
  }
}
