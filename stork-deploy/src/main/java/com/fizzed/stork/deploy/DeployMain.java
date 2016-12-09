/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.stork.deploy;

import com.fizzed.stork.core.BaseApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;

public class DeployMain extends BaseApplication {
    
    static public void main(String[] args) {
        new DeployMain().run(new ArrayDeque<>(Arrays.asList(args)));
    }

    @Override
    public void printUsage() {
        System.out.println("Usage: stork-deploy [OPTIONS] -a <file> <target>...");
        System.out.println("");
        System.out.println("Deploy a stork-compatible assembly to one or more targets.");
        System.out.println("Target supports ssh://host or vagrant+ssh://name formats");
        System.out.println("");
        System.out.println("Arguments");
        System.out.println("");
        System.out.println(" -v, --version           Print version and exit");
        System.out.println(" -h, --help              Print help and exit");
        System.out.println(" -a, --assembly <file>   Assembly file (.tar.gz or .zip)");
        System.out.println(" --verify                Verify only (do not deploy)");
        System.out.println(" --config-file <file>    Config file for deploy options");
        System.out.println(" --prefix-dir <dir>      Root directory to deploy in");
        System.out.println(" --user <user>           User that owns deploy dir");
        System.out.println(" --group <group>         Group that owns deploy dir");
        System.out.println(" --unattended            Do not prompt for answers during deploy");
        System.out.println("");
    }
    
    @Override
    public void run(Deque<String> args) {
        Path assemblyFile = null;
        List<String> targets = new ArrayList<>();
        DeployOptions argsOptions = new DeployOptions();
        DeployOptions configFileOptions = null;
        boolean verify = false;
        
        while (!args.isEmpty()) {
            String arg = args.remove();

            switch (arg) {
                case "-v":
                case "--version": {
                    System.out.println("stork-deploy " + com.fizzed.stork.core.Version.getLongVersion());
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
                case "-a":
                case "--assembly": {
                    assemblyFile = Paths.get(nextArg(arg, args));
                    break;
                }
                case "--verify": {
                    verify = true;
                    break;
                }
                case "--config-file": {
                    Path configFile = Paths.get(nextArg(arg, args));
                    try {
                        configFileOptions = DeployOptions.from(configFile);
                    } catch (IOException e) {
                        printErrorThenHelpHintAndExit(e.getMessage());
                    }
                    break;
                }
                case "--prefix-dir": {
                    argsOptions.prefixDir(nextArg(arg, args));
                    break;
                }
                case "--organization": {
                    argsOptions.organization(nextArg(arg, args));
                    break;
                }
                case "--user": {
                    argsOptions.user(nextArg(arg, args));
                    break;
                }
                case "--group": {
                    argsOptions.group(nextArg(arg, args));
                    break;
                }
                case "--unattended": {
                    argsOptions.unattended(true);
                    break;
                }
                default: {
                    if (arg.startsWith("-")) {
                        System.err.println("Invalid argument [" + arg + "]");
                        System.exit(1);
                    } else {
                        targets.add(arg);
                        break;
                    }
                }
            }
        }

        if (assemblyFile == null) {
            printErrorThenHelpHintAndExit("assembly file is required");
        }
        
        if (!Files.exists(assemblyFile)) {
            printErrorThenHelpHintAndExit("assembly file '" + assemblyFile + "' does not exist!");
        }
        
        if (targets.isEmpty()) {
            printErrorThenHelpHintAndExit("at least one target is required");
        }
        
        final Logger log = this.getLogger();
        logWelcomeMessage();
        
        try {
            // defaults THEN configFile THEN arguments
            DeployOptions options = new DeployOptions();
            options.overlay(configFileOptions);
            options.overlay(argsOptions);
            
            try (Assembly assembly = Assemblys.process(assemblyFile)) {
                for (String target : targets) {
                    if (verify) {
                        new Deployer().verify(assembly, options, target);
                        log.info("Review summary above!");
                    } else {
                        new Deployer().deploy(assembly, options, target);
                        log.info("Deployed!");
                    }
                }
            }
        } catch (Exception e) {
            // serious enough to dump a stack trace
            log.error("Unable to cleanly deploy", e);
            printErrorThenHelpHintAndExit(e.getMessage());
        }
    }
    
}
