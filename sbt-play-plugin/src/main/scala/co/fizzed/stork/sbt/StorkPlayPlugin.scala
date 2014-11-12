package co.fizzed.stork.sbt

import sbt._
import sbt.classpath._
import Keys._
import plugins.JvmPlugin

import co.fizzed.stork.launcher._
import co.fizzed.stork.bootstrap._
import org.apache.commons.io.FileUtils
import co.fizzed.stork.util.AssemblyUtils
import scala.collection.JavaConverters._
import org.slf4j.impl._
import play.Play
import play.PlayImport.PlayKeys._

object StorkPlayPlugin extends AutoPlugin {

    // loading after JvmPlugin ensures certain settings are not overwritten
    //override def requires = JvmPlugin
    override def requires = Play
    override def trigger = allRequirements

    object autoImport {
        lazy val storkPlayLauncherConf = settingKey[File]("Application specific overrides against default stork play config file.")
        lazy val storkPlayBootstrapConf = settingKey[File]("System property overrides dynamically loaded at runtime from config file.")
        lazy val storkAssemblyStageDir = settingKey[File]("Directory to stage stork assembly.")

        lazy val storkPlayAssembly = TaskKey[Unit]("stork-assembly", "Stages project and creates a stork assembly tarball")
        lazy val storkPlayBootstrap = TaskKey[Unit]("stork-bootstrap", "Applies conf/stork-bootstrap.conf system properties")
    }

    import autoImport._

    override val projectSettings = Seq(

        storkPlayLauncherConf := {
            baseDirectory(_ / "conf" / "stork-launcher.yml").value
        },

        storkPlayBootstrapConf := {
            baseDirectory(_ / "conf" / "stork-bootstrap.conf").value
        },

        storkAssemblyStageDir := {
            target(_ / "stork").value
        },

        // stork bootstrap lib required for running via PlayBootstrap in prod
        // inject the dependency here so it will be staged
        libraryDependencies += "co.fizzed" % "fizzed-stork-bootstrap" % co.fizzed.stork.bootstrap.Version.getVersion(),

        storkPlayBootstrap := {
            val log = streams.value.log
            val bootstrapConf = storkPlayBootstrapConf.value
            if (bootstrapConf.exists()) {
                log.info("Loading stork bootstrap from " + bootstrapConf);
                // load current sys props, load new ones to override, then set back
                val props = new java.util.Properties(System.getProperties())
                props.load(new java.io.FileInputStream(bootstrapConf));
                System.setProperties(props);
            } else {
                log.debug("Skipping stork bootstrap (file not present: " + bootstrapConf + ")");
            }
        },

        // bind bootstrap to "run" task
        run in Compile <<= (run in Compile).dependsOn(storkPlayBootstrap),

        storkPlayAssembly := {
            val log = streams.value.log
            log.info("Staging and assembly stork tarball...")

            try {
                // try to bind sbt logger for sbt-slf4j plugin (it may not exist if excluding it in play builds)
                StaticLoggerBinder.sbtLogger = streams.value.log
            } catch {
                // ignore all errors
                case unknown : Throwable => {}
            }

            // TODO: load these values from native packager and play plugins???
            // programmatically run stage task?

            // verify the "stage" task was previously run...
            val universalDir = target(_ / "universal" / "stage").value
            if (!universalDir.exists()) {
                throw new IllegalStateException("Universal stage dir " + universalDir + " does not exist. Did you forget to run \"stage\" task?")
            }

            //
            // generate launcher for play app (use default and merge with specific one if present)
            //
            val appLauncherConfFile = storkPlayLauncherConf.value
            val defaultLauncherConfFile = target(_ / "stork-launcher-default.yml").value
            PlayBootstrap.generateDefaultLauncherConfFile(defaultLauncherConfFile, name.value, organization.value, "conf/" + storkPlayBootstrapConf.value.getName(), "conf/logger.xml");
            var launcherConfFile = defaultLauncherConfFile

            if (appLauncherConfFile.exists()) {
                log.info("Merging base launcher config file for play apps with your application specific config")
                val merger = new Merger();
                val mergedLauncherFile = target(_ / "stork-launcher-merged.yml").value
                merger.merge(java.util.Arrays.asList(defaultLauncherConfFile, appLauncherConfFile), mergedLauncherFile);
                launcherConfFile = mergedLauncherFile
            } else {
                log.info("Using default launcher config file for play apps")
            }

            val stageDir = storkAssemblyStageDir.value
            stageDir.mkdirs()

            //
            // generate launcher configs (that will be used later on)
            //
            log.info("Generating play launcher to: " + stageDir);
            val generator = new Generator()
            val configs = generator.readConfigurationFiles(java.util.Arrays.asList(launcherConfFile))
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

            // create default stork-bootstrap.conf if needed
            val bootstrapConfName = storkPlayBootstrapConf.value.getName()
            val playStageBootstrapConfFile = new File(playStageConfDir, bootstrapConfName);
            if (playStageBootstrapConfFile.exists()) {
                log.info("Using existing bootstrap file: " + playStageBootstrapConfFile);
            } else {
                val bootstrapConfFile = new File(stageConfDir, bootstrapConfName);				
                log.info("Creating default bootstrap file: " + bootstrapConfFile);
                PlayBootstrap.generateDefaultBootstrapConfFile(bootstrapConfFile);
            }

            // create default logger.xml if needed
            val playStageLoggerConfFile = new File(playStageConfDir, "logger.xml");
            if (playStageLoggerConfFile.exists()) {
                log.info("Using existing logger.xml file: " + playStageLoggerConfFile);
            } else {
                val loggerConfFile = new File(stageConfDir, "logger.xml");				
                log.info("Creating default logger.xml file: " + loggerConfFile);
                PlayBootstrap.generateDefaultLoggerConfFile(loggerConfFile, playAppName);
            }

            // copy standard project resources (e.g. readme*, license*, changelog*, release* files)
            AssemblyUtils.copyStandardProjectResources(baseDirectory.value, stageDir);

            // tarball it up
            val tgzFile = AssemblyUtils.createTGZ(target.value, stageDir, assemblyName);
            log.info("Generated play stork assembly: " + tgzFile);
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
