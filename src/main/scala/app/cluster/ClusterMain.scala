package app.cluster

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import app.remoting.backend.BackendApp
import app.remoting.frontend.FrontendApp
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.io.StdIn
import concurrent.duration._
import scala.concurrent.Await
import app.Utils._

object ClusterMain extends App {
  private val log = LoggerFactory.getLogger(this.getClass.getName)

  args.toList match {
    case "seed" :: initialSeedNode :: Nil =>
      val initialSeed = initialSeedNode.toBoolean

      if (initialSeed) log.info("Starting as first seed-node") else log.info("Starting as seed-node")

      if (initialSeed) {
        val seed = createSeed(Some(6000))
        formCluster(seed)
      } else {
        createSeed(Some(6001))
      }

      StdIn.readLine()
      System.exit(0)

    case "frontend" :: Nil =>
      log.info("Starting as frontend-node")
      val frontend = new FrontendApp()
      Await.result(frontend.start(), 30 seconds)

      StdIn.readLine()
      System.exit(0)

    case "backend" :: Nil =>
      log.info("Starting as backend-node")
      val backEnd = new BackendApp()
      backEnd.start()

      StdIn.readLine()
      System.exit(0)

    case _ =>
      log.info("Starting as cluster with default ports")

      val systemSeed1 = createSeed(None)
      formCluster(systemSeed1)
      val systemSeed2 = createSeed(Some(6001))

      val backEnd = new BackendApp()
      backEnd.start()

      val frontend = new FrontendApp()
      Await.result(frontend.start(), 30 seconds)

      StdIn.readLine()
      System.exit(0)

  }

  private def createSeed(port: Option[Int]) = {
    val seedConfig: Config = port match {
      case None => ConfigFactory.load("seed")
      case Some(p) =>
        ConfigFactory.load("seed").withValue("akka.remote.netty.tcp.port", p)
    }
    ActorSystem("accountInsights", seedConfig)
  }

  def formCluster(seedActorSystem: ActorSystem) = {
    val cluster = Cluster(seedActorSystem)
    cluster.registerOnMemberUp {
      def members = cluster.state.members.filter(_.status == MemberStatus.Up)
      log.info("Current nodes in cluster: " + members.mkString("\n",", \n", "\n"))
    }
    cluster
  }
}
