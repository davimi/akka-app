package app.singlenode

import scala.io.StdIn

object Main extends App {

  val app = new AccountInsightsSingleNodeApp()

  app.start()

  StdIn.readLine()
  app.close()
  System.exit(0)
}
