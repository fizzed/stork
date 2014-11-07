package co.fizzed.stork.sbt

import sbt._
import sbt.classpath._
import Keys._

import co.fizzed.stork.launcher._
import scala.collection.JavaConverters._
import org.slf4j.impl._

object StorkPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    lazy val storkLauncherGenerate = taskKey[Unit]("Generates stork launchers")
  }

  import autoImport._

  override val projectSettings = Seq(
    storkLauncherGenerate := { 
      val log = streams.value.log

      try {
        // try to bind sbt logger for sbt-slf4j plugin (it may not exist if excluding it in play builds)
        StaticLoggerBinder.sbtLogger = streams.value.log
      } catch {
        case unknown : Throwable => {}
      }

      log.info("Running stork launcher generate...")

      val defaultInputDir = baseDirectory(_ / "src/main/launchers").value
      val inputFiles = List(defaultInputDir)
      val outputDir = target(_ / "stage").value

      val generator = new Generator()
      val allInputFiles = FileUtil.findAllFiles(inputFiles.map(f => f.getPath()).asJava, true)
      val configs = generator.readConfigurationFiles(allInputFiles)
      val generated = generator.generateAll(configs, outputDir)
      
      log.info("Generated " + generated + " stork launchers")
    }
  )
}
