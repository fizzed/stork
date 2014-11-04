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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class Stager extends BaseMavenApplication {

    @Override
    public void printUsage() {
        System.err.println("Usage: stork-maven-stage [-p <mavenProjectDir>]");
        System.err.println("-v                      Print version and exit");
        System.err.println("-p <mavenProjectDir>    Dir of play project");
    }

    static public void main(String[] args) {
        new Stager().run(args);
    }
    
    @Override
    public void run(String[] args) {
        List<String> argList = new ArrayList<String>(Arrays.asList(args));

        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-v") || argSwitch.equals("--version")) {
                System.err.println("stork-maven-assembly version: " + co.fizzed.stork.maven.Version.getLongVersion());
                System.exit(0);
            } else if (argSwitch.equals("-p") || argSwitch.equals("--play-project-dir")) {
                String fileString = popNextArg(argSwitch, argList);
                mavenProjectDir = new File(fileString);
            } else if (argSwitch.equals("-h") || argSwitch.equals("--help") ) {
                printUsage();
                System.exit(0);
            } else {
                printErrorThenUsageAndExit("invalid argument switch [" + argSwitch + "] found");
            }
        }

        try {
            stage();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
}
