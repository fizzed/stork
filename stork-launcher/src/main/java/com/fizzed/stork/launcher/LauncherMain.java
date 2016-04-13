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

import com.fizzed.stork.core.ArgumentException;
import com.fizzed.stork.core.BaseApplication;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;

/**
 * Main entry point for creating console/daemon launchers
 * 
 * @author joelauer
 */
public class LauncherMain extends BaseApplication {
    
    static public void main(String[] args) {
        new LauncherMain().run(args);
    }
    
    @Override
    public void printUsage() {
        System.out.println("Usage: stork-launcher -o <dir> <input>...");
        System.out.println("");
        System.out.println("Create console/daemon launchers for your Java apps. Input may");
        System.out.println("either be a file or include a wildcard to support searching.");
        System.out.println("");
        System.out.println("Arguments");
        System.out.println("");
        System.out.println(" -v, --version            Print version and exit");
        System.out.println(" -h, --help               Print help and exit");
        System.out.println(" -o, --output-dir <dir>   Output directory");
        System.out.println("");
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
                    System.out.println("stork-launcher " + com.fizzed.stork.core.Version.getLongVersion());
                    System.out.println(" by Fizzed, Inc. (http://fizzed.com)");
                    System.out.println(" at https://github.com/fizzed/stork");
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
                    outputDir = new File(nextArg(arg, args));
                    break;
                }
                default: {
                    if (arg.startsWith("-")) {
                        printErrorThenHelpHintAndExit("invalid argument [" + arg + "]");
                    } else {
                        configFileStrings.add(arg);
                        break;
                    }
                }
            }
        }

        if (outputDir == null) {
            printErrorThenHelpHintAndExit("output dir required");
        }
        
        if (configFileStrings.isEmpty()) {
            printErrorThenHelpHintAndExit("input file required");
        }
        
        final Logger log = this.getLogger();
        logWelcomeMessage();
        
        try {
            List<File> configFiles = FileUtil.findAllFiles(configFileStrings, false);
            ConfigurationFactory configFactory = new ConfigurationFactory();
            List<Configuration> configs = configFactory.read(configFiles);
            int generated = new Generator().generate(configs, outputDir);
            log.info("Created " + generated + " launchers");
        } catch (ArgumentException e) {
            printErrorThenHelpHintAndExit(e.getMessage());
        } catch (IOException e) {
            // serious enough to dump a stack trace
            log.error("Unable to cleanly generate launcher(s)", e);
            printErrorThenHelpHintAndExit(e.getMessage());
        }
    }
}
