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
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for creating console/daemon launchers
 * 
 * @author joelauer
 */
public class LauncherMain extends BaseApplication {
    static private final Logger logger = LoggerFactory.getLogger(LauncherMain.class);

    @Override
    public void printUsage() {
        System.err.println("Usage: stork-launcher -o <dir> <input>...");
        System.err.println("");
        System.err.println("Create console/daemon launchers for your Java apps. Input may");
        System.err.println("either be a file or include a wildcard to support searching.");
        System.err.println("");
        System.err.println("Arguments");
        System.err.println(" -v, --version            Print version and exit");
        System.err.println(" -o, --output-dir <dir>   Output directory");
    }

    static public void main(String[] args) {
        new LauncherMain().run(args);
    }

    @Override
    public void run(Deque<String> args) {
        File outputDir = null;
        List<String> configFileStrings = new ArrayList<>();
        
        while (!args.isEmpty()) {
            String arg = args.remove();

            switch (arg) {
                case "-v":
                case "--version": {
                    System.err.println("stork-launcher: v" + com.fizzed.stork.launcher.Version.getLongVersion());
                    System.exit(0);
                    break;
                }
                case "-h":
                case "--help": {
                    printUsage();
                    System.exit(0);
                    break;
                }
                case "-o":
                case "--output-dir": {
                    outputDir = new File(popNextArg(arg, args));
                    break;
                }
                default: {
                    if (arg.startsWith("-")) {
                        printErrorThenUsageAndExit("invalid argument [" + arg + "]");
                    } else {
                        configFileStrings.add(arg);
                        break;
                    }
                }
            }
        }

        if (outputDir == null) {
            printErrorThenUsageAndExit("output dir required");
        }
        
        if (configFileStrings.isEmpty()) {
            printErrorThenUsageAndExit("input file required");
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
