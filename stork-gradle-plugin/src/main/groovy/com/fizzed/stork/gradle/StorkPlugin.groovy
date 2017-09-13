package com.fizzed.stork.gradle

import com.fizzed.stork.gradle.tasks.AssemblyExtension
import com.fizzed.stork.gradle.tasks.AssemblyTask
import com.fizzed.stork.gradle.tasks.LaunchersExtension
import com.fizzed.stork.gradle.tasks.LaunchersTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.util.GradleVersion

class StorkPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (GradleVersion.current() < GradleVersion.version("4.0")) {
            throw new GradleException("Stork plugin require Gradle 4.0 or later")
        }

        LaunchersExtension lext = project.extensions.create("storkLaunchers", LaunchersExtension, project)
        LaunchersTask launchersTask = project.tasks.create(LaunchersTask.TASK_NAME, LaunchersTask) {
            description = "Generate Stork launchers scripts"
            group = "stork"
            outputDirectory = lext.outputDirectory
            inputFiles = lext.inputFiles
            launchers = lext.launchers
        }

        AssemblyExtension aext = project.extensions.create("storkAssembly", AssemblyExtension, project)
        Copy prepareAssembly = project.tasks.create("storkPrepareAssembly", Copy) {
            group("stork")
            dependsOn launchersTask
        }
        prepareAssembly.destinationDir = aext.stageDirectory
        prepareAssembly.with(configureAssemblySpec(project))

        AssemblyTask assemblyTask = project.tasks.create(AssemblyTask.TASK_NAME, AssemblyTask) {
            description = "Create stork assembly archive"
            group = "stork"
            outputDirectory = aext.outputDirectory
            stageDirectory = aext.stageDirectory
            filename = aext.filename
            dependsOn prepareAssembly
        }
    }

    private CopySpec configureAssemblySpec(Project project) {
        project.copySpec() {
            from(new File(project.projectDir, "src/assembly"))

            into("lib") {
                from(project.getTasksByName("jar", true))
                from(project.configurations.runtime)
            }
        }
    }
}
