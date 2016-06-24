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

import java.util.Optional;
import java.util.UUID;

public class Deployment {

    private final String uuid;
    private final String baseDir;
    private final String currentDir;
    private final String versionDir;
    private final Optional<String> user;
    private final Optional<String> group;

    public Deployment(String baseDir, String currentDir, String versionDir, String user, String group) {
        this.uuid = UUID.randomUUID().toString();
        this.baseDir = baseDir;
        this.currentDir = currentDir;
        this.versionDir = versionDir;
        this.user = Optional.ofNullable(user);
        this.group = Optional.ofNullable(group);
    }

    public String getUuid() {
        return uuid;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public String getVersionDir() {
        return versionDir;
    }

    public Optional<String> getUser() {
        return user;
    }
    
    public Optional<String> getGroup() {
        return group;
    }

    public Optional<String> getOwner() {
        if (!this.user.isPresent()) {
            return Optional.empty();
        } else {
            if (this.group.isPresent()) {
                return Optional.of(this.user.get() + ":" + this.group.get());
            } else {
                return this.user;
            }
        }
    }

}