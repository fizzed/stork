package com.fizzed.stork.gradle

import com.fizzed.stork.gradle.tasks.LaunchersTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Paths

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsEqual.equalTo
import static org.junit.Assert.assertTrue

class StorkPluginTests {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
    def pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

    File buildFile

    @Before
    void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle")
    }

    @Test
    void testBasic() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")
        project.pluginManager.apply 'com.fizzed.stork'

        assertTrue(project.tasks.storkLaunchers instanceof LaunchersTask)
    }

    @Test
    void testLauncherDsl() {
        buildFile << """
            plugins {
                id 'java'
                id 'com.fizzed.stork'
            }

            storkLaunchers {
                launcher {
                    name =  "test"
                    displayName = "test"
                    domain = "com.dcode.asgard"
                    shortDescription = "desc"
                    type = "DAEMON"
                    platforms = ["LINUX","MAC_OSX"]
                    workingDirMode = "APP_HOME"
                    mainClass="class"
                }
            }
        """

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath(pluginClasspath)
            .withArguments(LaunchersTask.TASK_NAME, "--stacktrace")
            .build()

        assertThat(result.task(":" + LaunchersTask.TASK_NAME).outcome, equalTo(SUCCESS))
        //check if script has been created
        assertTrue(Paths.get(testProjectDir.root.toString(), "build", "stork", "bin", "test").toFile().exists())
    }
}
