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
package com.fizzed.stork.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class GeneratorMain extends BaseApplication {
    static private final Logger logger = LoggerFactory.getLogger(GeneratorMain.class);

    @Override
    public void printUsage() {
        System.err.println("Usage: stork-generate -i <input config> -o <output directory> [-i ...]");
        System.err.println("-v                      Print version and exit");
        System.err.println("-i <input config>       Input file (dir or wildcard accepted)");
        System.err.println("-o <output directory>   Output directory");
    }

    static public void main(String[] args) {
        new GeneratorMain().run(args);
    }

    @Override
    public void run(String[] args) {
        if (args.length <= 0) {
            printErrorThenUsageAndExit("required parameters missing");
        }

        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        List<String> configFileStrings = new ArrayList<String>();
        File outputDir = null;

        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-v") || argSwitch.equals("--version")) {
                System.err.println("stork-generate: v" + com.fizzed.stork.launcher.Version.getLongVersion());
                System.exit(0);
            } else if (argSwitch.equals("-h") || argSwitch.equals("--help")) {
                printUsage();
                System.exit(0);
            } else if (argSwitch.equals("-i") || argSwitch.equals("--input")) {
                String fileString = popNextArg(argSwitch, argList);
                configFileStrings.add(fileString);
            } else if (argSwitch.equals("-o") || argSwitch.equals("--output")) {
                outputDir = new File(popNextArg(argSwitch, argList));
            } else {
                printErrorThenUsageAndExit("invalid argument switch [" + argSwitch + "] found");
            }
        }

        try {
            Generator generator = new Generator();
            List<File> configFiles = FileUtil.findAllFiles(configFileStrings, false);
            List<Configuration> configs = generator.readConfigurationFiles(configFiles);
            int generated = generator.generateAll(configs, outputDir);
            logger.info("Done (generated " + generated + " launchers)");
        } catch (ArgumentException e) {
            printErrorThenUsageAndExit(e.getMessage());
        } catch (IOException e) {
            // serious enough to dump a stack trace
            logger.error("Unable to cleanly process launcher configs", e);
            printErrorThenUsageAndExit(e.getMessage());
        }
    }
}
