package app.singlenode

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.scalatest._
import HttpMethods.POST

import scala.concurrent.Future

class AccountInsightsSingleNodeAppSpec
  extends AsyncWordSpec
    with Matchers
    with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    app.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    app.close()
  }

  val app = new AccountInsightsSingleNodeApp()

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val port = ConfigFactory.load().getInt("akka.http.server.port")
  val defaultAccount = "NL1234"

  "The AccountInsightsApp" should {
    "aggregate transactions for a given account" in {
      val transactionJson = """{"transactionId":"1234","amount":300.5,"accountNumber":"NL1234","time":1526724383307}"""
      val transactionJson2 = """{"transactionId": "1234","amount":300.5,"accountNumber":"NL1234","time":1526724383307}"""

      val createResponseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/accounts/create/", method = POST, entity = HttpEntity(defaultAccount)))
      createResponseFuture.map { res => res.status should equal(StatusCodes.OK) }

      val transaction1ResponseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/accounts/processtransaction/", entity = HttpEntity(ContentTypes.`application/json`, transactionJson), method = HttpMethods.POST))
      val transaction2ResponseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/accounts/processtransaction/", entity = HttpEntity(ContentTypes.`application/json`, transactionJson2), method = HttpMethods.POST))
      transaction1ResponseFuture.map { res => res.status should equal(StatusCodes.OK) }
      transaction2ResponseFuture.map { res => res.status should equal(StatusCodes.OK) }

      Thread.sleep(500)
      val monthlyBalanceResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$port/accounts/$defaultAccount/transactiontotal/monthly"))
      monthlyBalanceResponse.map { res => res.status should equal(StatusCodes.OK) }
    }
  }
}
