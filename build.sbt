packagedArtifacts in file(".") := Map.empty // disable publishing of root project

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.chain",
  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://gitlab.com/ubirch/ubirchChainService"),
    "scm:git:https://gitlab.com/ubirch/ubirchChainService.git"
  )),
  version := "0.1-SNAPSHOT"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(server, share, model)

lazy val server = project
  .settings(commonSettings: _*)
  .dependsOn(share, model)
  .settings(

    resolvers ++= Seq(
      Resolver.bintrayRepo("hseeberger", "maven"),
      Resolver.sonatypeRepo("snapshots")
    ),

    libraryDependencies ++= depServer

  )

lazy val model = project
  .settings(commonSettings: _*)
  .settings(

    libraryDependencies ++= {
      Seq(
        "joda-time" % "joda-time" % "2.9.4"
      )
    }

  )

lazy val share = project
  .settings(commonSettings: _*)

val akkaV = "2.4.9-RC2"
val scalaTestV = "3.0.0"
val json4sV = "3.4.0"
val configV = "1.3.0"

lazy val depServer = Seq(

  //akka
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,

  //testing
  "org.scalatest" %% "scalatest" % scalaTestV % "test",
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

  // app config
  "com.typesafe" % "config" % configV,

  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",

  // ubirch
  "com.ubirch.util" %% "crypto" % "0.2-SNAPSHOT",
  "com.ubirch.notary" %% "client" % "0.3.0-SNAPSHOT"

)

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
