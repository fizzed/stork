/*
 * Copyright 2014 mfizz.
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
package com.mfizz.jtools.launcher;

/*
 * #%L
 * mfz-jtools-launcher
 * %%
 * Copyright (C) 2012 - 2014 mfizz
 * %%
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
 * #L%
 */

import java.util.List;

/**
 *
 * @author joelauer
 */
public abstract class BaseApplication {

    public abstract void printUsage();
    
    public void printError(String errorMessage) {
        System.err.println("Error: " + errorMessage);
    }

    public void printUsageAndExitWithError() {
        printUsage();
        System.exit(1);
    }

    public void printErrorThenUsageAndExit(String errorMessage) {
        printError(errorMessage);
        printUsageAndExitWithError();
    }

    public String popNextArg(String argSwitch, List<String> argList) {
        if (argList.isEmpty()) {
            printErrorThenUsageAndExit("argument switch [" + argSwitch + "] is missing value as next argument");
        }
        return argList.remove(0);
    }
    
    public abstract void run(String[] args);

    /**
    static public void main(String[] args) {
        if (args.length <= 0) {
            printErrorThenUsageAndExit("required parameters missing");
        }

        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        List<File> configFiles = new ArrayList<File>();
        File outputFile = null;

        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-v")) {
                System.err.println("jtools-launcher-merge version: " + com.mfizz.jtools.launcher.Version.getLongVersion());
                System.exit(0);
            } else if (argSwitch.equals("-i")) {
                File configFile = new File(popNextArg(argSwitch, argList));
                if (!configFile.exists() || !configFile.canRead()) {
                    printErrorThenUsageAndExit("input config file [" + configFile + "] does not exist or is not readable");
                }
                configFiles.add(configFile);
            } else if (argSwitch.equals("-o")) {
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

        // validate required arguments
        if (configFiles.isEmpty()) {
            printErrorThenUsageAndExit("no input config files were specified");
        }

        if (outputFile == null) {
            printErrorThenUsageAndExit("no output file was specified");
        }

        
        ObjectMapper om = ConfigurationFactory.createObjectMapper();
        JsonNode mergedNode = null;
        
        // parse each configuration file into a configuration object
        List<Configuration> configs = new ArrayList<Configuration>();
        for (File configFile : configFiles) {
            try {
                JsonNode updateNode = om.readTree(configFile);
                if (mergedNode == null) {
                    mergedNode = updateNode;
                } else {
                    mergedNode = merge(mergedNode, updateNode);
                }
            } catch (Exception e) {
                printError("config file [" + configFile + "] invalid");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }

        
        try {
            // write merged file back out
            om.writeValue(System.out, mergedNode);
        } catch (Exception e) {
            printError("Unable to cleanly write merged config");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }

    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            }
            else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }
        }
        return mainNode;
    }
    */

}
