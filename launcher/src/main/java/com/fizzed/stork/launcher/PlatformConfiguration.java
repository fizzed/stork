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

import com.fizzed.stork.launcher.Configuration.DaemonMethod;

/**
 *
 * @author joelauer
 */
public class PlatformConfiguration {
    
    private Configuration.DaemonMethod daemonMethod;
    private String user;
    private String group;
    private String prefixDir;
    private String logDir;
    private String runDir;

    public DaemonMethod getDaemonMethod() {
        return daemonMethod;
    }

    public void setDaemonMethod(DaemonMethod daemonMethod) {
        this.daemonMethod = daemonMethod;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPrefixDir() {
        return prefixDir;
    }

    public void setPrefixDir(String prefixDir) {
        this.prefixDir = prefixDir;
    }
    
    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getRunDir() {
        return runDir;
    }

    public void setRunDir(String runDir) {
        this.runDir = runDir;
    }
    
}
