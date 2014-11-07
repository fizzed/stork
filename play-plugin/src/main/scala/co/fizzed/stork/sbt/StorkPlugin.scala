package co.fizzed.stork.sbt

import sbt._
import sbt.classpath._
import Keys._
import plugins.JvmPlugin

import co.fizzed.stork.launcher._
import co.fizzed.stork.bootstrap._
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.FileUtils
import co.fizzed.stork.util.TarUtils
import scala.collection.JavaConverters._
import org.slf4j.impl._

object StorkPlugin extends AutoPlugin {

	override def requires = JvmPlugin
	override def trigger = allRequirements

	object autoImport {
		lazy val storkAssembly = TaskKey[Unit]("stork-assembly", "Stages project and creates a stork assembly tarball")
		lazy val storkRun = TaskKey[Unit]("stork-run", "Runs play app with system properties loaded from conf/play.conf")
	}	

	import autoImport._

	override val projectSettings = Seq(

		// stork bootstrap lib required for running PlayBootstrap as oppposed to NettyServer
		libraryDependencies += "co.fizzed" % "fizzed-stork-bootstrap" % co.fizzed.stork.bootstrap.Version.getVersion(),

		storkRun := {
			println("Setting sys props before run...")
			System.setProperty("http.port", "9001");
		},

		storkAssembly := {
			val log = streams.value.log
			log.info("Staging and assembly stork tarball...")

			try {
				// try to bind sbt logger for sbt-slf4j plugin (it may not exist if excluding it in play builds)
				StaticLoggerBinder.sbtLogger = streams.value.log
			} catch {
				// ignore all errors
				case unknown : Throwable => {}
			}
			
			// TODO: load these values from native packager and play plugins...

			// verify the "stage" task was previously run...
			val universalDir = target(_ / "universal" / "stage").value
			if (!universalDir.exists()) {
				throw new IllegalStateException("Universal stage dir " + universalDir + " does not exist. Did you forget to run \"stage\" task?")
			}

			val launcherFile = baseDirectory(_ / "conf" / "launcher.yml").value
			val baseLauncherFile = PlayBootstrap.createBaseLauncherConfFile(target.value, name.value);
			var mergedLauncherFile = target(_ / "play-launcher-merged.yml").value

			if (launcherFile.exists()) {
				log.info("Merging base launcher config file for play apps with your application specific config")
		        val merger = new Merger();
		        merger.merge(java.util.Arrays.asList(baseLauncherFile, launcherFile), mergedLauncherFile);
			} else {
				log.info("Using base launcher config file for play apps")
				mergedLauncherFile = baseLauncherFile
			}

			val stageDir = target(_ / "stage").value
			stageDir.mkdirs()
			
			//
		    // generate launcher configs (that will be used later on)
		    //
		    log.info("Generating play launcher...");
			val generator = new Generator()
			val configs = generator.readConfigurationFiles(java.util.Arrays.asList(mergedLauncherFile))
			val generated = generator.generateAll(configs, stageDir)
			log.info("Successfully generated play launcher")

			val config = configs.get(0)

		    // playAppName may have been overridden
			var playAppName = name.value
		    if (playAppName != config.getName()) {
		        log.info("Launcher config file overrides play app name from [" + playAppName + "] to [" + config.getName() + "]")
		        playAppName = config.getName();
		    }


			val assemblyName = playAppName + "-" + version.value;
		    
		    val playStageLibDir = new File(universalDir, "lib");
		    val stageLibDir = new File(stageDir, "lib");
		    log.info("Copying " + playStageLibDir + " -> " + stageLibDir);
		    FileUtils.copyDirectory(playStageLibDir, stageLibDir);

		    val playStageConfDir = new File(universalDir, "conf");
		    val stageConfDir = new File(stageDir, "conf");
		    log.info("Copying " + playStageConfDir + " -> " + stageConfDir);
		    FileUtils.copyDirectory(playStageConfDir, stageConfDir);

		    val loggerConfFile = new File(stageConfDir, "logger.xml");
		    if (loggerConfFile.exists()) {
		        log.info("Using existing logger.xml file copied directly from conf dir...");
		    } else {
		        log.info("Creating default logger.xml file...");
		        PlayBootstrap.createLoggerConfFile(loggerConfFile, playAppName);
		    }

		    // create tarball
		    val tgzFile = new File(target.value, assemblyName + ".tar.gz");
		    val tgzout = TarUtils.createTGZStream(tgzFile);
		    try {
		        TarUtils.addFileToTGZStream(tgzout, stageDir.getAbsolutePath(), assemblyName, false);
		    } finally {
		        if (tgzout != null) {
		            tgzout.close();
		        }
		    }

		    log.info("Generated stork play assembly: " + tgzFile);
	


			/**
			// generate artifacts from packageBin
			val exportedProducts = (packageBin in Runtime).value
			log.info("Exported products: " + exportedProducts.toString())

			//val classpath = runTask(fullClasspath in Runtime, append(Seq(exportJars := true)))

			// generate classpath for external dependencies (list of artifacts)
			val classpath = (fullClasspath in Runtime).value
			for (entry <- classpath) {
				val a = entry.get(artifact.key)
				val f = entry.data
				log.info("Artifact: " + a.toString())
				log.info("    file: " + f)
			}
			*/

			/**
			log.info("Running stork launcher generate...")

			val defaultInputDir = baseDirectory(_ / "src/main/launchers").value
			val inputFiles = List(defaultInputDir)
			val outputDir = target(_ / "stage").value

			// all heavy lifting done by generator (just need to setup params)
			val generator = new Generator()
			val allInputFiles = FileUtil.findAllFiles(inputFiles.map(f => f.getPath()).asJava, true)
			val configs = generator.readConfigurationFiles(allInputFiles)
			val generated = generator.generateAll(configs, outputDir)

			log.info("Generated " + generated + " stork launchers")
			*/
		}

		/**
    	storkLauncherGenerate := { 
			val log = streams.value.log

			try {
				// try to bind sbt logger for sbt-slf4j plugin (it may not exist if excluding it in play builds)
				StaticLoggerBinder.sbtLogger = streams.value.log
			} catch {
				// ignore all errors
				case unknown : Throwable => {}
			}

			log.info("Running stork launcher generate...")

			val defaultInputDir = baseDirectory(_ / "src/main/launchers").value
			val inputFiles = List(defaultInputDir)
			val outputDir = target(_ / "stage").value

			// all heavy lifting done by generator (just need to setup params)
			val generator = new Generator()
			val allInputFiles = FileUtil.findAllFiles(inputFiles.map(f => f.getPath()).asJava, true)
			val configs = generator.readConfigurationFiles(allInputFiles)
			val generated = generator.generateAll(configs, outputDir)

			log.info("Generated " + generated + " stork launchers")
		}
		*/

		/**
		,
		//compile in Compile <<= (compile in Compile).dependsOn(storkLauncherGenerate)
		compile in Compile <<= (compile in Compile) map { result =>
			println("About to generate launchers...")
		 	storkLauncherGenerate
			result
		}

		,
		packageBin in Compile <<= (packageBin in Compile, externalDependencyClasspath in Runtime).map { (a, cp) =>
		  	println("project/compile::packageBin: " + a)
			println(cp)
		  a
		}
		*/
	)
}
