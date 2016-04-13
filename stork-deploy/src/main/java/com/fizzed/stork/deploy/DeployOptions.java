/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.stork.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DeployOptions {

    private String prefixDir;
    private String organization;
    private String user;
    private String group;

    public DeployOptions() {
        // no defaults!
    }
    
    public String getPrefixDir() {
        return prefixDir;
    }

    public DeployOptions prefixDir(String prefixDir) {
        this.prefixDir = prefixDir;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public DeployOptions organization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DeployOptions user(String user) {
        this.user = user;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public DeployOptions group(String group) {
        this.group = group;
        return this;
    }

    public void overlay(DeployOptions options) {
        if (options == null) { return; }
        if (options.prefixDir != null) { this.prefixDir = options.prefixDir; }
        if (options.organization != null) { this.organization = options.organization; }
        if (options.user != null) { this.user = options.user; }
        if (options.group != null) { this.group = options.group; }
    }
    
    static public DeployOptions from(Path file) throws IOException {
        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(file)) {
            properties.load(is);
        }
        return from(properties);
    }
    
    static public DeployOptions from(Properties properties) {
        DeployOptions options = new DeployOptions();
        options.prefixDir = properties.getProperty("prefix.dir");
        options.organization = properties.getProperty("organization");
        options.user = properties.getProperty("user");
        options.group = properties.getProperty("group");
        return options;
    }
    
}
