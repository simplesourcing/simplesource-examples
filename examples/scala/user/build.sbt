import sbt.Resolver
// The simplest possible sbt build file is just one line:

scalaVersion := "2.12.7"

name := "user"
organization := "io.simplesource"
version := "0.1.0"

val circeV = "0.9.3"
val avro4sV = "2.0.0-M1"
val simpleSourcingV = "0.1.4-SNAPSHOT"

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts (Artifact(
      "javax.ws.rs-api",
      "jar",
      "jar")),
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.typelevel" %% "cats-core" % "1.1.0",
    "io.simplesource" % "simplesource-command-api" % simpleSourcingV,
    "io.simplesource" % "simplesource-command-kafka" % simpleSourcingV,
    "io.simplesource" % "simplesource-command-serialization" % simpleSourcingV,
    "com.sksamuel.avro4s" %% "avro4s-core" % avro4sV,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "io.circe" %% "circe-java8" % circeV
  )
)

resolvers in Global ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "confluent" at "http://packages.confluent.io/maven/"
)

scalacOptions in ThisBuild := Seq(
  // following two lines must be "together"
  "-encoding",
  "UTF-8",
  "-Xlint",
  "-Xlint:missing-interpolator",
  //"-Xlog-implicits", // enable when trying to debug implicits
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Yno-adapted-args",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ywarn-value-discard",
  // "-Ywarn-unused-import", // seems to be broken for some imports [2.11]
  //"-Ypartial-unification", // enable once we go scala 2.12, fixes si-2712
  // "-Ywarn-unused", // broken in frontned [2.11]
  "-Ywarn-numeric-widen"
)

// ---- kind projector to have cleaner type lambdas ----
val kindProjectorPlugin = Seq(
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
)

val commonSettings = kindProjectorPlugin

lazy val model =
  Project(id = "model", base = file("modules/model"))
    .settings(commonSettings)

lazy val user =
  Project(id = "user", base = file("modules/user"))
    .settings(commonSettings, dependencies)
    .dependsOn(model)
