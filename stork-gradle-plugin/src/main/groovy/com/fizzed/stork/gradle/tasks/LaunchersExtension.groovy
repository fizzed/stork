package com.fizzed.stork.gradle.tasks

import org.gradle.api.Project

class LaunchersExtension {

    final Project project

    File outputDirectory

    List<LauncherExtension> launchers = new ArrayList<>()

    List<String> inputFiles

    LaunchersExtension(Project project) {
        this.project = project
        this.outputDirectory = new File(project.buildDir, "stork")
        this.inputFiles = ["${project.projectDir}/src/main/launchers".toString()]
    }

    LauncherExtension launcher(Closure closure) {
        LauncherExtension cfg  = project.configure(new LauncherExtension(), closure)
        println("launcher "+ cfg)
        launchers.add(cfg)
        cfg
    }
}
