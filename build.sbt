packagedArtifacts in file(".") := Map.empty // disable publishing of root/default project

lazy val testConfiguration = "-Dconfig.resource=" + Option(System.getProperty("test.config")).getOrElse("application.dev.conf")

lazy val commonSettings = Seq(

  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.chain",

  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://gitlab.com/ubirch/ubirchChainService"),
    "scm:git:https://gitlab.com/ubirch/ubirchChainService.git"
  )),
  version := "0.1-SNAPSHOT",

  resolvers ++= Seq(
    Resolver.bintrayRepo("hseeberger", "maven"),
    Resolver.sonatypeRepo("snapshots")
  ),

  javaOptions in Test += testConfiguration,
  fork in Test := true,
  // in ThisBuild is important to run tests of each subproject sequential instead parallelizing them
  testOptions in ThisBuild ++= Seq(
    Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
    Tests.Argument(TestFrameworks.ScalaTest, "-o")
  )

)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(server, core, share, model)

lazy val server = project
  .settings(commonSettings: _*)
  .dependsOn(share, model, core)
  .settings(
    libraryDependencies ++= depServer
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(model)
  .settings(
    libraryDependencies ++= depCore
  )

lazy val model = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += depJodaTime
  )

lazy val share = project
  .settings(commonSettings: _*)

val akkaV = "2.4.9-RC2"
val scalaTestV = "3.0.0"
val json4sV = "3.4.0"
val configV = "1.3.0"
val notaryServiceV = "0.3.0-SNAPSHOT"
val storageServiceV = "0.0.1-SNAPSHOT"
val ubirchUtilCryptoV = "0.2-SNAPSHOT"

lazy val depServer = Seq(

  //akka
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,

  //testing
  depScalaTest,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",

  //json4s
  "org.json4s" %% "json4s-core" % json4sV,
  "org.json4s" %% "json4s-native" % json4sV,
  "org.json4s" %% "json4s-ast" % json4sV,
  "org.json4s" %% "json4s-core" % json4sV,
  "org.json4s" %% "json4s-jackson" % json4sV,
  "org.json4s" %% "json4s-ext" % json4sV,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.8.0",

  // logging
  depTypesafeScalaLogging,
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12"

)

lazy val depCore = Seq(
  depTypesafeConfig,
  depTypesafeScalaLogging,
  depJodaTime,
  depUbirchUtilCrypto,
  depUbirchNotaryClient,
  depUbirchStorageClient,
  depScalaTest,
  depUbirchStorageTestUtil
)

lazy val depTypesafeConfig = "com.typesafe" % "config" % configV

lazy val depTypesafeScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

lazy val depJodaTime = "joda-time" % "joda-time" % "2.9.4"

lazy val depScalaTest = "org.scalatest" %% "scalatest" % scalaTestV % "test"

lazy val depUbirchNotaryClient = "com.ubirch.notary" %% "client" % notaryServiceV

lazy val depUbirchStorageClient = "com.ubirch.backend.storage" %% "client" % storageServiceV

lazy val depUbirchUtilCrypto = "com.ubirch.util" %% "crypto" % ubirchUtilCryptoV

lazy val depUbirchStorageTestUtil = "com.ubirch.backend.storage" %% "test-util" % storageServiceV % "test"

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case "application.conf" => MergeStrategy.concat
    case "application.base.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)
