name := """hello-server-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "co.fizzed" % "fizzed-stork-bootstrap" % "1.1.0"
)

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
