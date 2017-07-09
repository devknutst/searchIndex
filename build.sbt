name := """scala-dci"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

//resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.+",
  guice
)

