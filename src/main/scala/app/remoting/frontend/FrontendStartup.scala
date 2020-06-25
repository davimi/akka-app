package app.remoting.frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import app.RequestTimeout

import scala.concurrent.Future

trait FrontendStartup extends RequestTimeout {

  def startup(api: Route)(implicit actorSystem: ActorSystem): Future[ServerBinding] = {
    val host = actorSystem.settings.config.getString("akka.http.server.host")
    val port = actorSystem.settings.config.getInt("akka.http.server.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)(implicit actorSystem: ActorSystem): Future[ServerBinding] = {
    implicit val ec = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    Http().bindAndHandle(api, host, port) //Starts the HTTP server
  }
}
