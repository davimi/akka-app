import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings

scalaVersion := "2.12.4"

val akkaVersion = "2.5.14"
val akkaHttpVersion = "10.1.3"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream" % "2.5.12",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.12" % Test,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",

    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",

    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "joda-time" % "joda-time" % "2.9.9",

    //logging
    "com.typesafe.akka" %%  "akka-slf4j" % akkaVersion,
    "ch.qos.logback" %  "logback-classic" % "1.2.3"
  )
}

val commonSettings = Seq(
  name := "akkaapp",
  version := "0.2",
  scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps", "language:implicitConversions"),
  test in assembly := {}, // do not run any test when assembling

  parallelExecution in Test := false
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm) // load the multi-jvm configuration
  .settings(multiJvmSettings: _*) // apply the default settings
  .settings(
    mainClass in run := Some("app.cluster.ClusterMain"),
    mainClass in assembly := Some("app.cluster.ClusterMain"),
    assemblyJarName in assembly := "akkaapp_cluster.jar"
  )
