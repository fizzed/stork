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

import com.fizzed.stork.core.ArgumentException;
import com.fizzed.stork.launcher.Configuration.DaemonMethod;
import com.fizzed.stork.launcher.Configuration.Platform;
import com.fizzed.stork.launcher.Configuration.Type;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates launchers.
 * 
 * @author joelauer
 */
public class Generator {
    static private final Logger logger = LoggerFactory.getLogger(Generator.class);
    
    public Generator() {
        // do nothing
    }
    
    private Path canonicalPath(File f) {
        /**
        Path workingDir = Paths.get(".");
        Path file = f.toPath();
        return workingDir.relativize(file);
        */
        return f.toPath().normalize();
    }
    
    public int generate(Configuration config, File outputDir) throws ArgumentException, IOException {
        return generate(Arrays.asList(config), outputDir);
    }
    
    public int generate(List<Configuration> configs, File outputDir) throws ArgumentException, IOException {
        // validate output directory
        if (outputDir == null) {
            throw new ArgumentException("No output dir was specified");
        }
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new ArgumentException("Unable to crate output directory [" + outputDir + "]");
            } else {
                logger.info("Created dir {}", canonicalPath(outputDir));
            }
        }
        if (!outputDir.isDirectory()) {
            throw new ArgumentException("Output directory [" + outputDir + "] exists but is not a directory");
        }
        if (!outputDir.canWrite()) {
            throw new ArgumentException("Output directory [" + outputDir + "] is not writable");
        }

        if (configs.isEmpty()) {
            logger.warn("No input configuration files (no stork launchers will be generated)");
            return 0;
        }
        
        // use each configuration object to generate one or more launchers
        int generated = 0;
        for (Configuration config : configs) {
            try {
                logger.info("Launcher {}", canonicalPath(config.getFile()));
                doGenerate(config, outputDir);
                generated++;
            } catch (Exception e) {
                throw new IOException("Unable to cleanly generate launcher for [" + config.getFile() + "]", e);
            }
        }
        return generated;
    }
    
    
    static private freemarker.template.Configuration fmconfig;
    static private freemarker.template.Configuration getOrCreateFreemarker() throws Exception {
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

    private void doGenerate(Configuration config, File outputDir) throws ArgumentException, Exception {
        Platform unixLauncherGeneratedVia = null;
        File binDir = Paths.get(outputDir.getPath(), config.getBinDir()).toFile();
        File shareDir = Paths.get(outputDir.getPath(), config.getShareDir()).toFile();

        // sort platforms by name
        TreeSet<Platform> sortedPlatforms = new TreeSet<>(config.getPlatforms());

        // generate for each platform
        for (Configuration.Platform platform : sortedPlatforms) {
            logger.info(" {}", platform);

            // create launcher model to render
            LauncherModel model = new LauncherModel(config);

            if (platform == Platform.LINUX || platform == Platform.MAC_OSX) {

                if (unixLauncherGeneratedVia != null) {
                    // no need to generate again
                    logger.info("  script same as {}", unixLauncherGeneratedVia);
                } else {
                    // generate unix launcher script
                    binDir.mkdirs();
                    File launcherFile = new File(binDir, config.getName());

                    if (config.getType() == Type.CONSOLE) {
                        generateUnixConsoleLauncher(config, launcherFile, model);
                    } else if (config.getType() == Type.DAEMON) {
                        generateUnixDaemonLauncher(config, launcherFile, model);
                    }
                    
                    if (config.isIncludeJavaDetectHelper()) {
                        File helperDir = new File(shareDir, "helper");
                        helperDir.mkdirs();
                        
                        File javaDetectFile = new File(helperDir, "java-detect");
                        generateUnixJavaDetectScript(javaDetectFile);
                    }
                    
                    unixLauncherGeneratedVia = platform;
                }
                
                if (platform == Platform.LINUX && config.getType() == Type.DAEMON) {
                    // create init.d scripts
                    File initdDir = new File(shareDir, "init.d");
                    initdDir.mkdirs();
                    
                    // generate debian compatible init.d startup script
                    File initdFile = new File(initdDir, config.getName() + ".init");
                    generateInitdScript(config, initdFile, model);
                }
                
                if (platform == Platform.MAC_OSX && config.getType() == Type.DAEMON) {
                    // create init.d scripts
                    File osxDir = new File(shareDir, "osx");
                    osxDir.mkdirs();
                    
                    // generate osx compatible launchd script
                    File launchdFile = new File(osxDir, config.getDomain() + "." + config.getName() + ".plist");
                    generateOSXLaunchdScript(config, launchdFile, model);
                }
                
            } else if (platform == Platform.WINDOWS) {

                if (config.getType() == Type.CONSOLE) {
                    
                    // generate unix launcher script
                    binDir.mkdirs();
                    File launcherFile = new File(binDir, config.getName() + ".bat");
                    generateWindowsConsoleLauncher(config, launcherFile, model);
                            
                } else if (config.getType() == Type.DAEMON) {
                    
                    DaemonMethod dm = config.getPlatformDaemonMethod(Platform.WINDOWS);
                    switch (dm) {
                        case JSLWIN:
                            generateWindowsJSLWinLauncher(config, binDir, model);
                            break;
                        case WINSW:
                            generateWindowsWINSWLauncher(config, binDir, model);
                            break;
                        default:
                            throw new ArgumentException("Unsupported daemon method [" + dm + "] for platform WINDOWS");
                    }
                }

            } else {
                throw new ArgumentException("Unsupported platform " + platform);
            }
        }
    }

    private void generateUnixConsoleLauncher(Configuration config, File launcherFile, LauncherModel model) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(launcherFile)) {
            try (Writer out = new OutputStreamWriter(fos)) {
                processTemplate("linux/script-header.ftl", out, model);
                includeResource("linux/script-functions.sh", fos);
                processTemplate("linux/script-java.ftl", out, model);
                processTemplate("linux/script-console.ftl", out, model);
                // set to executable
                launcherFile.setExecutable(true);
                logger.info("  script {}", launcherFile);
            }
        }
    }
    
    private void generateUnixDaemonLauncher(Configuration config, File launcherFile, LauncherModel model) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(launcherFile)) {
            try (Writer out = new OutputStreamWriter(fos)) {
                processTemplate("linux/script-header.ftl", out, model);
                includeResource("linux/script-functions.sh", fos);
                processTemplate("linux/script-java.ftl", out, model);
                
                DaemonMethod dm = config.getPlatformDaemonMethod(Platform.LINUX);

                if (dm == DaemonMethod.NOHUP) {
                    processTemplate("linux/script-daemon-nohup.ftl", out, model);
                } else {
                    throw new Exception("Unsupported daemon method [" + dm + "] for platform LINUX");
                }

                launcherFile.setExecutable(true);
                logger.info("  script {}", launcherFile);
            }
        }
    }
    
    private void generateInitdScript(Configuration config, File initdFile, LauncherModel model) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(initdFile)) {
            try (Writer out = new OutputStreamWriter(fos)) {
                processTemplate("linux/initd-daemon.ftl", out, model);
                initdFile.setExecutable(true);
                logger.info("  init.d {}", initdFile);
            }
        }
    }
    
    private void generateOSXLaunchdScript(Configuration config, File launchdFile, LauncherModel model) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(launchdFile)) {
            try (Writer out = new OutputStreamWriter(fos)) {
                processTemplate("osx/launchd.ftl", out, model);
                logger.info("  launchd {}", launchdFile);
            }
        }
    }
    
    
    private void generateUnixJavaDetectScript(File file) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try (Writer out = new OutputStreamWriter(fos)) {
                includeResource("linux/script-java-detect-header.sh", fos);
                includeResource("linux/script-functions.sh", fos);
                includeResource("linux/script-java-detect.sh", fos);
                file.setExecutable(true);
                logger.info("  helper {}", file);
            }
        }
    }
    
    
    private void generateWindowsConsoleLauncher(Configuration config, File launcherFile, LauncherModel model) throws Exception {
        // make sure parent of file to be generated exists
        FileOutputStream fos = new FileOutputStream(launcherFile);
        Writer out = new OutputStreamWriter(fos);

        try {
            processTemplate("windows/batch-header.ftl", out, model);
            
            includeResource("windows/batch-find-java.bat", fos);

            processTemplate("windows/batch-java.ftl", out, model);
            
            processTemplate("windows/batch-console.ftl", out, model);

            processTemplate("windows/batch-footer.ftl", out, model);
            
            // set to executable
            launcherFile.setExecutable(true);

            logger.info("  script {}", launcherFile);
        } finally {
            if (out != null) {
                out.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    private void generateWindowsWINSWLauncher(Configuration config, File binDir, LauncherModel model) throws Exception {
        binDir.mkdirs();
        
        File serviceFile = new File(binDir, config.getName() + ".exe");
        File configFile = new File(binDir, config.getName() + ".xml");
        File netFile = new File(binDir, config.getName() + ".exe.config");
        
        copyResource("windows/winsw/winsw-1.16-bin.exe", serviceFile);
        logger.info("  created {}", serviceFile);
        
        copyResource("windows/winsw/winsw.exe.config", netFile);
        logger.info("  created {}", serviceFile);
        
        generateWindowsWINSWConfig(config, configFile, model);
    }
    
    private void generateWindowsWINSWConfig(Configuration config, File configFile, LauncherModel model) throws Exception {
        FileOutputStream fos = new FileOutputStream(configFile);
        Writer out = new OutputStreamWriter(fos);

        try {
            processTemplate("windows/config-daemon-winsw.ftl", out, model);
        } finally {
            if (out != null) {
                out.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    private void generateWindowsJSLWinLauncher(Configuration config, File binDir, LauncherModel model) throws Exception {
        binDir.mkdirs();
        
        File launcherFile = new File(binDir, config.getName() + ".bat");
        
        // 1 main launcher file required
        FileOutputStream fos = new FileOutputStream(launcherFile);
        Writer out = new OutputStreamWriter(fos);

        try {
            processTemplate("windows/batch-header.ftl", out, model);
            
            includeResource("windows/batch-find-java.bat", fos);

            processTemplate("windows/batch-java.ftl", out, model);
            
            processTemplate("windows/batch-daemon-jslwin.ftl", out, model);

            processTemplate("windows/batch-footer.ftl", out, model);
            
            // set to executable
            launcherFile.setExecutable(true);

            logger.info("  script {}", launcherFile);
        } finally {
            if (out != null) {
                out.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        
        // 4 files required: service.exe, service.ini, service64.exe, and service64.ini
        File serviceFile = new File(binDir, config.getName() + "32.exe");
        File iniFile = new File(binDir, config.getName() + "32.ini");
        File service64File = new File(binDir, config.getName() + "64.exe");
        File ini64File = new File(binDir, config.getName() + "64.ini");
        
        copyResource("windows/jslwin/jsl_static.exe", serviceFile);
        logger.info("  helper {}", serviceFile);
        
        generateWindowsJSLWinINI(config, iniFile, model);
        logger.info("  helper {}", iniFile);
        
        copyResource("windows/jslwin/jsl_static64.exe", service64File);
        logger.info("  helper {}", service64File);
        
        generateWindowsJSLWinINI(config, ini64File, model);
        logger.info("  helper {}", ini64File);
    }
    
    private void generateWindowsJSLWinINI(Configuration config, File iniFile, LauncherModel model) throws Exception {
        FileOutputStream fos = new FileOutputStream(iniFile);
        Writer out = new OutputStreamWriter(fos);

        try {
            processTemplate("windows/config-daemon-jslwin.ftl", out, model);
        } finally {
            if (out != null) {
                out.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    private void processTemplate(String templateName, Writer out, Object model) throws Exception {
        freemarker.template.Configuration freemarker = getOrCreateFreemarker();
        Template template = freemarker.getTemplate(templateName);
        template.process(model, out);
    }
    
    private void copyResource(String resourceName, File targetFile) throws Exception {
        FileOutputStream fos = new FileOutputStream(targetFile);
        try {
            includeResource(resourceName, fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private void includeResource(String resourceName, OutputStream os) throws Exception {
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
