import sbt._
import scala._
import Keys._
import scala.xml.{XML, Source}

object StorkPluginBuild extends Build {

  val pom = XML.load(Source.fromFile(new File("../pom.xml")))
  val storkVersion = (pom \ "version").text
  val storkGroupId = (pom \ "groupId").text

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.defaultSettings ++ Seq(
      sbtPlugin := true,
      name := "fizzed-stork-sbt-plugin",
      organization := storkGroupId,
      version := storkVersion,
      libraryDependencies += "co.fizzed" % "fizzed-stork-launcher" % storkVersion,
      // for bridging slf4j calls to sbt logging
      // https://github.com/eirslett/sbt-slf4j
      libraryDependencies += "com.github.eirslett" %% "sbt-slf4j" % "0.1"
    )
  )
}
