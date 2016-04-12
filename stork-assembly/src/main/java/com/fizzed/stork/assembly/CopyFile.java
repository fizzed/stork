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

package com.fizzed.stork.assembly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for copy files/dirs while retaining execute permissions.
 */
public class CopyFile {
    private static final Logger logger = LoggerFactory.getLogger(CopyFile.class);
    
    static public void main(String[] args) throws Exception {
        new CopyFile().run(args);
    }
    
    public void run(String[] args) {
        if (args.length < 2) {
            System.err.println("At least two arguments required");
            System.exit(1);
        }
        
        File outputDir = new File(args[0]);
        
        List<File> inputFiles = new ArrayList<>();
        
        for (int i = 1; i < args.length; i++) {
            inputFiles.add(new File(args[i]));
        }
        
        if (inputFiles.isEmpty()) {
            logger.error("No input file(s)/dir(s)");
            System.exit(1);
        }
        
        for (File inputFile : inputFiles) {
            try {
                if (inputFile.isDirectory()) {
                    File[] files = inputFile.listFiles();
                    for (File f : files) {
                        copyFile(f, outputDir);
                    }
                } else {
                    copyFile(inputFile, outputDir);
                }
            } catch (Exception e) {
                logger.error("", e);
                System.exit(1);
            }
        }
    }
    
    public void copyFile(File inputFile, File outputDir) throws IOException {
        if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles();
            File newOutputDir = new File(outputDir, inputFile.getName());
            for (File f : files) {
                copyFile(f, newOutputDir);
            }
        } else {
            File outputFile = new File(outputDir, inputFile.getName());
            outputDir.mkdirs();
            logger.info(" copying " + inputFile + " to " + outputFile);
            FileUtils.copyFile(inputFile, outputFile);
            if (inputFile.canExecute()) {
                outputFile.setExecutable(true);
            }
        }
    }
    
}
