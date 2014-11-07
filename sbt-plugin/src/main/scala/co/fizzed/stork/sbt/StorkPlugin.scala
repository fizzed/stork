package co.fizzed.stork.sbt

import sbt._
import sbt.classpath._
import Keys._

import co.fizzed.stork.launcher._
import scala.collection.JavaConverters._

object StorkPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    lazy val storkLauncherGenerate = taskKey[Unit]("Generates stork launchers")
  }

  import autoImport._

  override val projectSettings = Seq(
    storkLauncherGenerate := {
      println("Launcher generate task...")

      val inputFiles = List(new File("src/main/launchers"))
      val outputDir = new File("target/stage")

      // remove any non-existent directories from inputFiles
      val filteredInputFiles = inputFiles.filter(_.exists()).map(f => f.getPath())

      if (filteredInputFiles.length <= 0) {
        println("No inputFile dirs or files exist (skipping)")
      } else {
        val generator = new Generator()
        val configs = generator.createConfigsFromFileStrings(filteredInputFiles.asJava);
        generator.runConfigs(configs, outputDir);
      }
    }
  )
}
