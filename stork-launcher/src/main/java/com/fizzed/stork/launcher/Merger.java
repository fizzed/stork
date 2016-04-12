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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Merges config files together.
 * 
 * @author joelauer
 */
public class Merger {

    static public void merge(List<File> configFiles, File outputFile) throws IOException {
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
        for (File configFile : configFiles) {
            try {
                JsonNode updateNode = factory.createConfigNode(configFile);
                if (mergedNode == null) {
                    mergedNode = updateNode;
                } else {
                    mergedNode = factory.mergeNodes(mergedNode, updateNode);
                }
            } catch (Exception e) {
                throw new IOException("Config file [" + configFile + "] invalid");
            }
        }
        
        try {
            // write merged file back out
            factory.getMapper().writeValue(outputFile, mergedNode);
            System.out.println("Wrote merged config file: " + outputFile);
        } catch (Exception e) {
            throw new IOException("Unable to cleanly write merged config");
        }
    }

}
