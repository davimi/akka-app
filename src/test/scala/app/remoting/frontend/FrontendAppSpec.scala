package app.remoting.frontend

import akka.http.scaladsl.Http
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import app.Utils._

class FrontendAppSpec() extends WordSpecLike with Matchers {

  "The FrontEndApp" should {
    "start up" in {
      val conf = ConfigFactory.load("frontend")
        .withValue("akka.http.server.port", 2000 + Random.nextInt(6000))
        .withValue("akka.remote.netty.tcp.port", 2000 + Random.nextInt(6000))
      val frontEnd = new FrontendApp(conf)
      frontEnd.start().foreach {
         sb => sb shouldBe a[Http.ServerBinding]
      }
    }
  }
}
