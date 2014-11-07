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
    private File bootstrapFile;
    
    static public void main(String[] args) throws Exception {
        new Bootstrap().run(args);
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public File getBootstrapFile() {
        return bootstrapFile;
    }

    public void setBootstrapFile(File bootstrapFile) {
        this.bootstrapFile = bootstrapFile;
    }
    
    public void run(String[] args) throws Exception {
        if (System.getProperties().containsKey("launcher.main")) {
            this.mainClass = System.getProperty("launcher.main");
        }
        
        if (mainClass == null || mainClass.equals("")) {
            throw new Exception("Java system property [launcher.main] must be defined. Please set -Dlauncher.main=com.example.Main");
        }
        
        if (System.getProperties().containsKey("launcher.bootstrap")) {
            String bootstrapPath = System.getProperty("launcher.bootstrap");
            if (bootstrapPath != null && !bootstrapPath.equals("")) {
                this.bootstrapFile = new File(bootstrapPath);
                if (!bootstrapFile.exists()) {
                    throw new Exception("Java launcher.bootstrap [" + bootstrapFile + "] does not exist");
                }
                if (!bootstrapFile.isFile() || !bootstrapFile.canRead()) {
                    throw new Exception("Java launcher.bootstrap [" + bootstrapFile + "] is either not a file or not readable");
                }
            }
        }
        
        Properties props = new Properties(System.getProperties());
        
        if (bootstrapFile != null) {
            // create new properties using system properties as defaults
            loadLauncherConfig(props, bootstrapFile);
        }
        
        overrideSystemProperties(props);

        System.setProperties(props);
        
        String[] overriddenArgs = overrideArguments(args);
        
        runMain(mainClass, overriddenArgs);
    }
    
    public void loadLauncherConfig(Properties props, File configFile) throws Exception {
        System.out.println("Loading bootstrap properties: " + configFile);
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
