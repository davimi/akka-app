package app.cluster

import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec, MultiNodeSpecCallbacks}
import akka.testkit.ImplicitSender
import app.remoting.backend.BackendApp
import app.remoting.frontend.FrontendApp
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._


object WordsClusterSpecConfig extends MultiNodeConfig {
  val seed1 = role("seed") //why do they have to be unique? In normal config, seed nodes all have the same role name? // is this the same as akka.cluster.roles = ["seed"]?? //several seed nodes possible?
  val frontend = role("frontend")
  val backend = role("backend")

  commonConfig(ConfigFactory.parseString("""akka.actor.provider="cluster""""))
}

/**
  * Stitch akka multi node test framework together with ScalaTest
  */
trait ScalaTestMultiNodeSpec extends MultiNodeSpecCallbacks with WordSpecLike with Matchers with BeforeAndAfterAll {
  override def beforeAll() = multiNodeSpecBeforeAll()
  override def afterAll() = multiNodeSpecAfterAll()
}

//for multi JVM; Must be equal to number of nodes (?)
class WordsClusterSpecMultiJvmNode1 extends ClusterSpec
class WordsClusterSpecMultiJvmNode2 extends ClusterSpec
class WordsClusterSpecMultiJvmNode3 extends ClusterSpec


class ClusterSpec
  extends MultiNodeSpec(WordsClusterSpecConfig)
  with ScalaTestMultiNodeSpec
  with ImplicitSender {

  import WordsClusterSpecConfig._

  muteDeadLetters(classOf[Any])(system)

  override def initialParticipants: Int = 1

  def seedAddress: Address = node(seed1).address
  def frontEndAddress: Address = node(frontend).address
  def backEndAddress: Address = node(backend).address


  "The Cluster" should {
    val cluster = Cluster(system)
    "start the cluster" in within(20 seconds) {
      cluster.subscribe(testActor, classOf[MemberUp]) // subscribe the testActor to cluster events so we can tune in
      expectMsgClass(classOf[CurrentClusterState])

      cluster.join(seedAddress) // the config above does not contain a seed list, so we need to join the seeds manually
      expectMsgPF(){ case m: MemberUp => m.member.address should be(seedAddress)}

      cluster.join(frontEndAddress)
      expectMsgPF(){ case m: MemberUp => m.member.address should be(frontEndAddress)}

      cluster.join(backEndAddress)
      expectMsgPF(){ case m: MemberUp => m.member.address should be(backEndAddress)}

      enterBarrier("startUp")
    }

    "start the frontend" in within(10 seconds) {
      // without runOn, code will be executed on all nodes!
      runOn(frontend) {
        val frontendApp = new FrontendApp()
        Await.result(frontendApp.start(), 30 seconds)
      }
      enterBarrier("frontend")
    }

    "start the backend" in within(10 seconds){
      runOn(backend) {
        val backEnd = new BackendApp()
        backEnd.start()
      }
      enterBarrier("backend")
    }


  }
}
