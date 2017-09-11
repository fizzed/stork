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
import java.util.SortedSet;

public class ExistingDeployment extends Deployment {

    private final Long deployedAt;
    private final SortedSet<VersionedPath> versionDirs;
    private final Optional<Integer> retain;

    public ExistingDeployment(String baseDir, String currentDir, String versionDir, String user, String group, Long deployedAt, SortedSet<VersionedPath> versionDirs, Optional<Integer> retain) {
        super(baseDir, currentDir, versionDir, user, group);
        this.versionDirs = versionDirs;
        this.deployedAt = deployedAt;
        this.retain = retain;
    }

    public Long getDeployedAt() {
        return deployedAt;
    }

    public SortedSet<VersionedPath> getVersionDirs() {
        return versionDirs;
    }
    
    public boolean isFresh() {
        return getCurrentDir() == null;
    }
    
    public boolean isUpgrade() {
        return getCurrentDir() != null;
    }

    public Optional<Integer> getRetain() {
        return retain;
    }
    
}