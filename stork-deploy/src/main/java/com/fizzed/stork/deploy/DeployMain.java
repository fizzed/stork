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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class DeployMain {
    
    static public void main(String[] args) {
        new DeployMain().run(new ArrayDeque<>(Arrays.asList(args)));
    }

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
    }
    
    public void run(Deque<String> args) {
        Path assemblyFile = null;
        List<String> targets = new ArrayList<>();
        boolean verify = false;
        
        while (!args.isEmpty()) {
            String arg = args.remove();

            switch (arg) {
                case "-v":
                case "--version": {
                    System.out.println("stork-deploy: v" + com.fizzed.stork.deploy.Version.getLongVersion());
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
            printErrorAndExit("assembly file is required");
        }
        
        if (!Files.exists(assemblyFile)) {
            printErrorAndExit("assembly file '" + assemblyFile + "' does not exist!");
        }
        
        if (targets.isEmpty()) {
            printErrorAndExit("at least one target required");
        }
        
        try {
            try (Assembly assembly = Assemblys.process(assemblyFile)) {
                for (String target : targets) {
                    if (verify) {
                        new Deployer().verify(assembly, new Options(), target);
                    } else {
                        new Deployer().deploy(assembly, new Options(), target);
                    }
                }
            }
        } catch (IOException | DeployerException e) {
            printErrorAndExit(e.getMessage());
        }
    }
    
    public void printErrorAndExit(String message) {
        System.err.println("[ERROR] " + message);
        System.exit(1);
    }
    
    public String nextArg(String arg, Deque<String> args) {
        if (args.isEmpty()) {
            System.err.println("Argument [" + arg + "] requires a value as the next argument");
            System.exit(1);
        }
        return args.remove();
    }
    
}
