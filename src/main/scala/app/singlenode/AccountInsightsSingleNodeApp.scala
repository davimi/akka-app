package app.singlenode

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import api.AccountInsightsRestApi
import app.RequestTimeout
import com.typesafe.config.{Config, ConfigFactory}
import service.AccountInsightsApi

import scala.collection.JavaConverters._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

class AccountInsightsSingleNodeApp(config: Config = ConfigFactory.load())
  extends AccountInsightsRestApi
  with AccountInsightsApi
  with RequestTimeout {

  implicit val actorSystem: ActorSystem = ActorSystem("accountInsights")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val requestTimeout: Timeout = getRequestTimeout(config)

  private var api: Future[Http.ServerBinding] = _

  def start(): Future[Http.ServerBinding] = {
    api = Http(actorSystem).bindAndHandle(route, config.getString("akka.http.server.host"), config.getInt("akka.http.server.port"))
    api
  }

  def close(): Future[Done] = Await.result(api.map( _.unbind()), 20 seconds)
}