package com.fizzed.stork.gradle.tasks

import com.fizzed.stork.assembly.AssemblyUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AssemblyTask extends DefaultTask {

    public static final String TASK_NAME = "storkAssembly"

    File outputDirectory

    File stageDirectory

    String filename

    @TaskAction
    void action() {
        AssemblyUtils.copyStandardProjectResources(project.projectDir, stageDirectory)
        File tgzFile = AssemblyUtils.createTGZ(outputDirectory, stageDirectory, filename)
        logger.info("Generated maven stork assembly: ${tgzFile}")
    }
}
