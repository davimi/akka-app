package app.remoting.frontend

import actors.AccountHistoriesSupervisor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.http.scaladsl.Http
import akka.routing.ConsistentHashingPool
import akka.util.Timeout
import api.{AccountInsightsRestApi, ApiResponse}
import com.typesafe.config.{Config, ConfigFactory}
import messages.{CreateAccountManager, GetTransactionsByMonth, ProcessTransaction}
import org.slf4j.LoggerFactory
import service.AccountInsightsApi

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

class FrontendApp(frontendConfig: Config = ConfigFactory.load("frontend"))
  extends AccountInsightsRestApi
  with AccountInsightsApi
  with FrontendStartup {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit val actorSystem: ActorSystem = ActorSystem("accountInsights", frontendConfig)
  implicit val requestTimeout: Timeout = getRequestTimeout(frontendConfig)

  def start(): Future[Http.ServerBinding] = {
    val server = startup(route)
    server.onComplete {
      case Success(_) => log.info("Frontend started!")
      case Failure(e) => log.info("Frontend startup failed!" + e.getMessage)
    }
    server
  }

  override def createBackend(): ActorRef = {
    createWorkerRouter()
  }

  def createWorkerRouter(): ActorRef = {

    def routerHashFunction: PartialFunction[Any, String] = {
      case msg: CreateAccountManager => msg.accountNumber
      case msg: ProcessTransaction => msg.accountNumber
      case msg: GetTransactionsByMonth => msg.accountNumber
    }

    actorSystem.actorOf(
      ClusterRouterPool(ConsistentHashingPool(1, hashMapping = routerHashFunction), ClusterRouterPoolSettings(
        totalInstances = 100, maxInstancesPerNode = 20,
        allowLocalRoutees = false, useRoles = "backend")).props(Props[AccountHistoriesSupervisor]),
      name = "backend-router")
  }
}
