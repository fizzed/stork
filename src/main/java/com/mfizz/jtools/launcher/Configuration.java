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
    
    @JsonIgnore
    private File file;
    
    private String binDir = "bin";
    private String runDir = "run";
    private String shareDir = "share";
    
    @NotNull @Size(min=1)
    private Set<Platform> platforms;
    
    @NotNull
    private String name;

    @NotNull
    private String mainClass;
    
    @NotNull
    private Type type;
    
    private WorkingDirMode workingDirMode;
    private String appArgs = "";
    private String javaArgs = "";
    private String jarDir = "lib";
    private String minJavaVersion = "1.6";
    private Integer minJavaMemory = null;
    private Integer maxJavaMemory = null;
    private Integer minJavaMemoryPct = null;
    private Integer maxJavaMemoryPct = null;
    
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
    
}
