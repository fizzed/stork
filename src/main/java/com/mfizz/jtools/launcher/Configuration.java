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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Describes the launcher(s) to generate.
 * 
 * @author joelauer
 */
public class Configuration {
    
    static public enum Platform {
        LINUX,
        MAC_OSX,
        WINDOWS
    }
    
    static public enum Type {
        CONSOLE,
        DAEMON
    }
    
    static public enum WorkingDirMode {
        RETAIN,
        APP_HOME
    }
    
    static public enum DaemonMethod {
        NOHUP
    }
    
    @JsonIgnore
    private File file;
    
    // standard directories for app layout
    private String binDir = "bin";
    private String runDir = "run";
    private String shareDir = "share";
    private String logDir = "log";
    private String jarDir = "lib";
    
    @NotNull @Size(min=1)
    private Set<Platform> platforms;
    
    @NotNull
    private String name;
    
    @NotNull
    private String shortDescription;
    private String longDescription;

    @NotNull
    private String mainClass;
    
    @NotNull
    private Type type;
    
    private WorkingDirMode workingDirMode;
    private String appArgs = "";
    private String javaArgs = "";
    private String minJavaVersion = "1.6";
    private Integer minJavaMemory = null;
    private Integer maxJavaMemory = null;
    private Integer minJavaMemoryPct = null;
    private Integer maxJavaMemoryPct = null;
    
    // best effort to symlink java binary so process is named something more
    // friendly for users (only safe for daemons with unique names)
    // default name is "<app name>-java"
    private boolean symlinkJava = false;
    
    // http://stackoverflow.com/questions/958249/whats-the-difference-between-nohup-and-a-daemon
    // really intersting discussion of NOHUP vs. other methods of daemonizing
    private DaemonMethod daemonMethod = DaemonMethod.NOHUP;
    // daemon pid will be tested after this amount of seconds to confirm it is
    // still running -- a simple way to verify that it likely started
    private Integer daemonMinLifetime = 3;
    // daemon will print this line to stdout/stderr to announce it started successfully
    private String daemonLaunchConfirm = null;
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getBinDir() {
        return binDir;
    }

    public void setBinDir(String binDir) {
        this.binDir = binDir;
    }

    public String getShareDir() {
        return shareDir;
    }

    public void setShareDir(String shareDir) {
        this.shareDir = shareDir;
    }

    public String getRunDir() {
        return runDir;
    }

    public void setRunDir(String runDir) {
        this.runDir = runDir;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<Platform> platforms) {
        this.platforms = platforms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public WorkingDirMode getWorkingDirMode() {
        if (workingDirMode == null) {
            if (this.type == Type.CONSOLE) {
                return WorkingDirMode.RETAIN;
            } else if (this.type == Type.DAEMON) {
                return WorkingDirMode.APP_HOME;
            }
        }
        return workingDirMode;
    }

    public void setWorkingDirMode(WorkingDirMode workingDirMode) {
        this.workingDirMode = workingDirMode;
    }

    public String getAppArgs() {
        return appArgs;
    }

    public void setAppArgs(String appArgs) {
        this.appArgs = appArgs;
    }

    public String getJavaArgs() {
        return javaArgs;
    }

    public void setJavaArgs(String javaArgs) {
        this.javaArgs = javaArgs;
    }

    public String getJarDir() {
        return jarDir;
    }

    public void setJarDir(String jarDir) {
        this.jarDir = jarDir;
    }

    public String getMinJavaVersion() {
        return minJavaVersion;
    }

    public void setMinJavaVersion(String minJavaVersion) {
        this.minJavaVersion = minJavaVersion;
    }

    public Integer getMinJavaMemory() {
        return minJavaMemory;
    }

    public void setMinJavaMemory(Integer minJavaMemory) {
        this.minJavaMemory = minJavaMemory;
    }

    public Integer getMaxJavaMemory() {
        return maxJavaMemory;
    }

    public void setMaxJavaMemory(Integer maxJavaMemory) {
        this.maxJavaMemory = maxJavaMemory;
    }

    public Integer getMinJavaMemoryPct() {
        return minJavaMemoryPct;
    }

    public void setMinJavaMemoryPct(Integer minJavaMemoryPct) {
        this.minJavaMemoryPct = minJavaMemoryPct;
    }

    public Integer getMaxJavaMemoryPct() {
        return maxJavaMemoryPct;
    }

    public void setMaxJavaMemoryPct(Integer maxJavaMemoryPct) {
        this.maxJavaMemoryPct = maxJavaMemoryPct;
    }

    public boolean isSymlinkJava() {
        return symlinkJava;
    }

    public void setSymlinkJava(boolean symlinkJava) {
        this.symlinkJava = symlinkJava;
    }

    public DaemonMethod getDaemonMethod() {
        return daemonMethod;
    }

    public void setDaemonMethod(DaemonMethod daemonMethod) {
        this.daemonMethod = daemonMethod;
    }

    public Integer getDaemonMinLifetime() {
        return daemonMinLifetime;
    }

    public void setDaemonMinLifetime(Integer daemonMinLifetime) {
        this.daemonMinLifetime = daemonMinLifetime;
    }

    public String getDaemonLaunchConfirm() {
        return daemonLaunchConfirm;
    }

    public void setDaemonLaunchConfirm(String daemonLaunchConfirm) {
        this.daemonLaunchConfirm = daemonLaunchConfirm;
    }

}
