package com.fizzed.stork.gradle.tasks

import org.gradle.api.Project

class AssemblyExtension {

    final Project project

    File outputDirectory

    File stageDirectory

    String filename

    AssemblyExtension(Project project) {
        this.project = project
        this.outputDirectory = project.buildDir
        this.stageDirectory = new File(project.buildDir, "stork")
        this.filename = "${project.name}-${project.version}".toString()
    }
}
