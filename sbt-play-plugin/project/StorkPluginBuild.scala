import sbt._
import scala._
import Keys._
import Defaults._
import scala.xml.{XML, Source}

object StorkPluginBuild extends Build {
    // load version and group from project pom file
    val pom = XML.load(Source.fromFile(new File("../pom.xml")))
    val storkVersion = (pom \ "version").text
    val storkGroupId = (pom \ "groupId").text

    lazy val project = Project (
        "project",
        file ("."),
        settings = Defaults.defaultSettings ++ Seq(
            sbtPlugin := true,
            name := "fizzed-stork-sbt-play-plugin",
            organization := storkGroupId,
            version := storkVersion,
            libraryDependencies += "co.fizzed" % "fizzed-stork-util" % storkVersion,
            libraryDependencies += "co.fizzed" % "fizzed-stork-bootstrap" % storkVersion,
            libraryDependencies += "co.fizzed" % "fizzed-stork-launcher" % storkVersion,
            // for bridging slf4j calls to sbt logging
            // https://github.com/eirslett/sbt-slf4j
            libraryDependencies += "com.github.eirslett" %% "sbt-slf4j" % "0.1" % "provided",
	    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
	    //resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"
        )
	++ addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.6" % "provided")
    )
}
