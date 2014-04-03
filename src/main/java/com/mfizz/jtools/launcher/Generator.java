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

import com.mfizz.jtools.launcher.Configuration.Platform;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author joelauer
 */
public class Generator {
    
    static public void printUsage() {
        System.err.println("Usage: -i <input config> -o <output directory>");
    }
    
    static public void printError(String errorMessage) {
        System.err.println("Error: " + errorMessage);
    }
    
    static public void printUsageAndExitWithError() {
        printUsage();
        System.exit(1);
    }
    
    static public void printErrorThenUsageAndExit(String errorMessage) {
        printError(errorMessage);
        printUsageAndExitWithError();
    }
    
    static public String popNextArg(String argSwitch, List<String> argList) {
        if (argList.isEmpty()) {
            printErrorThenUsageAndExit("argument switch [" + argSwitch + "] is missing value as next argument");
        }
        return argList.remove(0);
    }
    
    static public void main(String[] args) {
        if (args.length <= 0) {
            printErrorThenUsageAndExit("required parameters missing");
        }
        
        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        List<File> configFiles = new ArrayList<File>();
        File outputDir = null;
        
        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);
            
            if (argSwitch.equals("-i")) {
                File configFile = new File(popNextArg(argSwitch, argList));
                if (!configFile.exists() || !configFile.canRead()) {
                    printErrorThenUsageAndExit("input config file [" + configFile + "] does not exist or is not readable");
                }
                configFiles.add(configFile);
            } else if (argSwitch.equals("-o")) {
                outputDir = new File(popNextArg(argSwitch, argList));
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
        
        if (outputDir == null) {
            printErrorThenUsageAndExit("no output dir was specified");
        }
        
        // parse each configuration file into a configuration object
        List<Configuration> configs = new ArrayList<Configuration>();
        for (File configFile : configFiles) {
            try {
                configs.add(ConfigurationFactory.create(configFile));
            } catch (Exception e) {
                printError("config file [" + configFile + "] invalid");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        
        // use each configuration object to generate one or more launchers
        for (Configuration config : configs) {
            try {
                System.out.println("Generating launcher for config: " + config.getFile());
                generate(config, outputDir);
            } catch (Exception e) {
                printError("Unable to generate launcher for config [" + config.getFile() + "]");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        
    }
    
    static private freemarker.template.Configuration fmconfig;
    static public freemarker.template.Configuration getOrCreateFreemarker() throws Exception {
        if (fmconfig != null) {
            return fmconfig;
        }
        
        /* Create and adjust the configuration */
        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        //cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
        cfg.setClassForTemplateLoading(Generator.class, "");
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        
        fmconfig = cfg;
        return fmconfig;
    }
    
    static public void generate(Configuration config, File outputDir) throws Exception {
        
        Platform unixLauncherGeneratedVia = null;
        
        // sort platforms by name
        TreeSet<Platform> sortedPlatforms = new TreeSet<Platform>(config.getPlatforms());
        
        // generate for each platform
        for (Configuration.Platform platform : sortedPlatforms) {
            System.out.println("Generating launcher for platform: " + platform);
            
            // create launcher model to render
            LauncherModel model = new LauncherModel(config);
            
            if (platform == Platform.LINUX || platform == Platform.MAC_OSX) {
                
                if (unixLauncherGeneratedVia != null) {
                    // no need to generate again
                    System.out.println(" - launcher: same as for " + unixLauncherGeneratedVia);
                } else {
                    // generate unix launcher script
                    File outputFile = new File(outputDir, config.getName());
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    Writer out = new OutputStreamWriter(fos);

                    try {
                        processTemplate("linux/script-header.ftl", out, model);

                        includeResource("linux/script-functions.sh", fos);

                        processTemplate("linux/script-java.ftl", out, model);

                        processTemplate("linux/script-console.ftl", out, model);

                        // set to executable
                        outputFile.setExecutable(true);

                        System.out.println(" - launcher: " + outputFile);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    }
                    
                    unixLauncherGeneratedVia = platform;
                }
            } else if (platform == Platform.WINDOWS) {
                
            } else {
                throw new Exception("Unsupported platform " + platform);
            }
        }
    }
    
    static public void processTemplate(String templateName, Writer out, Object model) throws Exception {
        freemarker.template.Configuration freemarker = getOrCreateFreemarker();
        Template template = freemarker.getTemplate(templateName);
        template.process(model, out);
    }
    
    static public void includeResource(String resourceName, OutputStream os) throws Exception {
        InputStream is = Generator.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new Exception("Unable to find resource " + resourceName);
        }
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
        }
        is.close();
    }
    
}
