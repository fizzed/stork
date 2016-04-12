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

public class Options {

    private String prefixDir;
    private String organization;
    private String user;
    private String group;

    public Options() {
        this.prefixDir = "/opt";
    }
    
    public String getPrefixDir() {
        return prefixDir;
    }

    public Options prefixDir(String prefixDir) {
        this.prefixDir = prefixDir;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public Options organization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getUser() {
        return user;
    }

    public Options user(String user) {
        this.user = user;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public Options group(String group) {
        this.group = group;
        return this;
    }

}
