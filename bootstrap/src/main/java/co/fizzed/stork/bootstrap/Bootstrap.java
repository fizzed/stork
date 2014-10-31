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
package co.fizzed.stork.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 *
 * @author joelauer
 */
public class Bootstrap {

    private String mainClass;
    private File configFile;
    
    static public void main(String[] args) throws Exception {
        new Bootstrap().run(args);
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
    
    public void run(String[] args) throws Exception {
        if (System.getProperties().containsKey("launcher.main")) {
            this.mainClass = System.getProperty("launcher.main");
        }
        
        if (mainClass == null || mainClass.equals("")) {
            throw new Exception("Java system property [launcher.main] must be defined. Please set -Dlauncher.main=com.example.Main");
        }
        
        if (System.getProperties().containsKey("launcher.config")) {
            String configPath = System.getProperty("launcher.config");
            if (configPath != null && !configPath.equals("")) {
                this.configFile = new File(configPath);
                if (!configFile.exists()) {
                    throw new Exception("Java launcher.config [" + configFile + "] does not exist");
                }
                if (!configFile.isFile() || !configFile.canRead()) {
                    throw new Exception("Java launcher.config [" + configFile + "] is either not a file or not readable");
                }
            }
        }
        
        Properties props = new Properties(System.getProperties());
        
        if (configFile != null) {
            // create new properties using system properties as defaults
            loadLauncherConfig(props, configFile);
        }
        
        overrideSystemProperties(props);

        System.setProperties(props);
        
        String[] overriddenArgs = overrideArguments(args);
        
        runMain(mainClass, overriddenArgs);
    }
    
    public void loadLauncherConfig(Properties props, File configFile) throws Exception {
        props.load(new FileInputStream(configFile));
    }
    
    public void overrideSystemProperties(Properties props) {
        // do nothing
    }
    
    public String[] overrideArguments(String[] args) {
        return args;
    }
    
    public void runMain(String mainClass, String[] args) throws Exception {
        // dynamically invoke main() method for play via NettyServer
        Class clazz = Class.forName(mainClass);
        Method method = clazz.getMethod("main", String[].class);
        method.invoke(null, (Object)args);
    }
    
}
