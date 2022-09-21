ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "CityPathFinder"
  )
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-shapes"
).map(_ % circeVersion)
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies += "org.specs2" %% "specs2-core" % "4.16.1" % Test
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.backuity.clist" %% "clist-core"   % "3.5.1"
libraryDependencies += "org.backuity.clist" %% "clist-macros" % "3.5.1" % "provided"
