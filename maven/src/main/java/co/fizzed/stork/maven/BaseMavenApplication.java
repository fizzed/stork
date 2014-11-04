/*
 * Copyright 2014 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.fizzed.stork.maven;

import co.fizzed.stork.launcher.BaseApplication;
import co.fizzed.stork.launcher.Configuration;
import co.fizzed.stork.launcher.Generator;
import co.fizzed.stork.launcher.Merger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public abstract class BaseMavenApplication extends BaseApplication {

    protected File mavenProjectDir;
    protected String mavenCommand;
    protected File targetDir;
    protected File stageDir;
    protected File jarFile;
    
    public BaseMavenApplication() {
        mavenProjectDir = new File(".");
    }

    public void stage() throws Exception {
        // verify project dir is a directory
        if (!mavenProjectDir.exists()) {
            printError("Maven project dir [" + mavenProjectDir + "] does not exist");
            System.exit(1);
        }

        if (!mavenProjectDir.isDirectory()) {
            printError("Maven project dir [" + mavenProjectDir + "] exists but is not a directory");
            System.exit(1);
        }

        // find maven command
        mavenCommand = findMavenCommand(mavenProjectDir);
        if (mavenCommand == null) {
            printError("Unable to find [mvn] command.  Installed and on PATH?");
            System.exit(1);
        }
        
        targetDir = new File(mavenProjectDir, "target");
        stageDir = new File(targetDir, "stage");

        //
        // stage project
        //
        
        // package project (better create a jar...)
        new ProcessExecutor()
            .directory(mavenProjectDir)
            .command(mavenCommand, "package")
            .readOutput(true)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValues(0)
            .execute();
        
        // target directory should contain exactly a single .jar
        File[] jars = targetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".jar")) {
                    return !pathname.getName().endsWith("-sources.jar");
                }
                return false;
            }
        });
        
        if (jars == null || jars.length == 0) {
            printError("Maven build directory must contain at least one .jar (it contained zero)");
            System.exit(1);
        }
        
        if (jars.length > 1) {
            printError("Maven build directory must contain one .jar (it contained " + jars.length + " .jar files)");
            System.exit(1);
        }
        
        jarFile = jars[0];
        
        // copy artifact jar to stage/lib
        File stageLibDir = new File(stageDir, "lib");
        
        FileUtils.copyFileToDirectory(jarFile, stageLibDir);
        
        // copy dependencies to target/stage/lib
        new ProcessExecutor()
            .directory(mavenProjectDir)
            .command(mavenCommand, "org.apache.maven.plugins:maven-dependency-plugin:2.9:copy-dependencies", "-DoutputDirectory=" + stageLibDir,"-DincludeScope=runtime","-Dmdep.prependGroupId=true")
            .readOutput(true)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValues(0)
            .execute();

        // copy bin/ conf/ share/ dirs into target/stage
        File binDir = new File(mavenProjectDir, "bin");
        if (binDir.exists()) {
            File stageBinDir = new File(stageDir, "bin");
            FileUtils.copyDirectory(binDir, stageBinDir);
        }
        
        File confDir = new File(mavenProjectDir, "conf");
        if (confDir.exists()) {
            File stageConfDir = new File(stageDir, "conf");
            FileUtils.copyDirectory(confDir, stageConfDir);
        }
        
        File shareDir = new File(mavenProjectDir, "share");
        if (shareDir.exists()) {
            File stageShareDir = new File(stageDir, "share");
            FileUtils.copyDirectory(shareDir, stageShareDir);
        }
        
        // copy README*, CHANGELOG*, RELEASE_NOTES*
        FileUtils.copyDirectory(mavenProjectDir, stageDir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName().toLowerCase();
                if (name.startsWith("readme") || name.startsWith("changelog") || name.startsWith("release_notes")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        System.out.println("Staged application at: " + stageDir);
        System.out.println("Done!");
    }
    

    static public String findMavenCommand(File mavenProjectDir) {
        // do we need to append ".bat" on the end if running on windows?
        String batEnd = "";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            batEnd = ".bat";
        }
        
        String mavenCommand = "mvn" + batEnd;
        
        try {
            ProcessResult result = new ProcessExecutor()
                .command(mavenCommand, "--version")
                .readOutput(true)
                .execute();
            System.out.println("Maven [" + mavenCommand + "] command found: " + result.outputUTF8().trim());
            return mavenCommand;
        } catch (Exception e) {
            System.out.println("Maven [" + mavenCommand + "] command not found");
            return null;
        }
    }

}
