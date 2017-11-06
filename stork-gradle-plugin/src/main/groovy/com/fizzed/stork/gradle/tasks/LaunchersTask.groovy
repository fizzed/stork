package com.fizzed.stork.gradle.tasks;

import com.fizzed.stork.core.ArgumentException
import com.fizzed.stork.launcher.Configuration
import com.fizzed.stork.launcher.ConfigurationFactory
import com.fizzed.stork.launcher.FileUtil
import com.fizzed.stork.launcher.Generator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task equivalent to maven plugin "stork-launcher"
 *
 * @author by BertrandA
 */
class LaunchersTask extends DefaultTask {

    public static final String TASK_NAME = "storkLaunchers"

    File outputDirectory

    List<String> inputFiles

    List<LauncherExtension> launchers

    @TaskAction
    void action() throws IOException {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                throw new IOException("Could not create directory:" + outputDirectory)
            }
        }

        try {
            def configFiles = FileUtil.findAllFiles(inputFiles, true)
            // read launchers from yml files
            List<Configuration> configs = new ConfigurationFactory().read(configFiles)
            // appends launchers configured into gradle build scripts
            println("configs: "+ configs)
            println("launchers: "+ launchers)
            launchers.each { configs.add(it.toConfiguration()) }

            println("configs: "+ configs)
            int generated = new Generator().generate(configs, outputDirectory)
            logger.info("generated ${generated} launcher(s)")
        } catch (ArgumentException | IOException e) {
            throw new GradleException(e.getMessage(), e)
        }
    }
}
