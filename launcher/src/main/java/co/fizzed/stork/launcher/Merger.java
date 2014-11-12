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
package co.fizzed.stork.launcher;

import com.fasterxml.jackson.databind.JsonNode;
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
public class Merger extends BaseApplication {
    private static final Logger logger = LoggerFactory.getLogger(Merger.class);

    @Override
    public void printUsage() {
        System.err.println("Usage: stork-merge -i <input config> -o <output config> [-i ...]");
        System.err.println("-v                   Print version and exit");
        System.err.println("-i <input config>    Input file (dir or wildcard accepted)");
        System.err.println("-o <output config>   Output file");
    }

    static public void main(String[] args) {
        new Merger().run(args);
    }
    
    @Override
    public void run(String[] args) {
        if (args.length <= 0) {
            printErrorThenUsageAndExit("required parameters missing");
        }

        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        List<File> configFiles = new ArrayList<File>();
        File outputFile = null;

        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-v") || argSwitch.equals("--version")) {
                System.err.println("stork-merge: v" + co.fizzed.stork.launcher.Version.getLongVersion());
                System.exit(0);
            } else if (argSwitch.equals("-h") || argSwitch.equals("--help")) {
                printUsage();
                System.exit(0);
            } else if (argSwitch.equals("-i") || argSwitch.equals("--input")) {
                String fileString = popNextArg(argSwitch, argList);
                try {
                    List<File> files = FileUtil.findFiles(fileString, false);
                    configFiles.addAll(files);
                } catch (IOException e) {
                    printErrorThenUsageAndExit(e.getMessage());
                }
            } else if (argSwitch.equals("-o") || argSwitch.equals("--output")) {
                outputFile = new File(popNextArg(argSwitch, argList));
                File outputDir = outputFile.getParentFile();
                if (!outputDir.exists()) {
                    if (!outputDir.mkdirs()) {
                        printErrorThenUsageAndExit("unable to make output dir [" + outputDir + "]");
                    } else {
                        System.out.println("Created output directory: " + outputDir);
                    }
                }
                if (!outputDir.isDirectory()) {
                    printErrorThenUsageAndExit("output dir [" + outputDir + "] exists but is not a directory");
                }
                if (!outputDir.canWrite()) {
                    printErrorThenUsageAndExit("output dir [" + outputDir + "] is not writable");
                }
            } else {
                printErrorThenUsageAndExit("invalid argument switch [" + argSwitch + "] found");
            }
        }

        try {
            merge(configFiles, outputFile);
        } catch (IOException e) {
            logger.error("Unable to cleanly merge launcher configs", e);
            printErrorThenUsageAndExit(e.getMessage());
        }
    }
    
    
    public void merge(List<File> configFiles, File outputFile) throws IOException {
        // validate required arguments
        if (configFiles == null || configFiles.isEmpty()) {
            throw new IllegalArgumentException("No input config files were found");
        }

        if (outputFile == null) {
            throw new IOException("no output file was specified");
        }

        ConfigurationFactory factory = new ConfigurationFactory();
        JsonNode mergedNode = null;
        
        // parse each configuration file into a configuration object
        List<Configuration> configs = new ArrayList<Configuration>();
        for (File configFile : configFiles) {
            try {
                JsonNode updateNode = factory.createConfigNode(configFile);
                if (mergedNode == null) {
                    mergedNode = updateNode;
                } else {
                    mergedNode = factory.mergeNodes(mergedNode, updateNode);
                }
            } catch (Exception e) {
                printError("config file [" + configFile + "] invalid");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        
        try {
            // write merged file back out
            factory.getMapper().writeValue(outputFile, mergedNode);
            System.out.println("Wrote merged config file: " + outputFile);
        } catch (Exception e) {
            printError("Unable to cleanly write merged config");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
