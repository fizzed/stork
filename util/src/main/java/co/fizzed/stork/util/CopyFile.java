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

package co.fizzed.stork.util;

import co.fizzed.stork.launcher.BaseApplication;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author joelauer
 */
public class CopyFile extends BaseApplication {
    
    static public void main(String[] args) throws Exception {
        new CopyFile().run(args);
    }

    @Override
    public void printUsage() {
        System.out.println("copy files...");
    }

    @Override
    public void run(String[] args) {
        List<String> argList = new ArrayList<String>(Arrays.asList(args));

        List<File> inputFiles = new ArrayList<File>();
        File outputDir = null;
        
        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-i")) {
                String fileString = popNextArg(argSwitch, argList);
                inputFiles.add(new File(fileString));
            } else if (argSwitch.equals("-o")) {
                String fileString = popNextArg(argSwitch, argList);
                outputDir = new File(fileString);
            } else {
                printErrorThenUsageAndExit("invalid argument switch [" + argSwitch + "] found");
            }
        }
        
        if (inputFiles.isEmpty()) {
            printErrorThenUsageAndExit("No input files provided");
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
                e.printStackTrace();
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
            System.out.println("Copying " + inputFile + " to " + outputFile);
            FileUtils.copyFile(inputFile, outputFile);
            if (inputFile.canExecute()) {
                outputFile.setExecutable(true);
            }
        }
    }
    
}
