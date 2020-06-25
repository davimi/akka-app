package actors

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

trait StopAkkaSystem extends BeforeAndAfterAll {
  this: TestKit with Suite => //can only be used together with a scalatest Suite and Akka TestKit
  override protected def afterAll(): Unit = {
    super.beforeAll()
    system.terminate()
  }
}
