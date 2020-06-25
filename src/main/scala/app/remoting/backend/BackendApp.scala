package app.remoting.backend

import actors.AccountHistoriesSupervisor
import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.RequestTimeout
import com.typesafe.config.{Config, ConfigFactory}

class BackendApp(backendConfig: Config = ConfigFactory.load("backend")) {

  def start(): ActorRef = {

    val system = ActorSystem("accountInsights", backendConfig)
    //implicit val requestTimeout: Timeout = getRequestTimeout(backendConfig)

    system.actorOf(AccountHistoriesSupervisor.props, AccountHistoriesSupervisor.name)
  }
}
