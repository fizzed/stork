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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Assembly implements Closeable {

    private final long createdAt;
    private final Path archiveFile;
    private final Path unpackedDir;
    private final String name;
    private final String version;
    private final boolean snapshot;
    private final Map<InitType,Set<Daemon>> daemons;
    private final List<Closeable> resources;

    public Assembly(Path archiveFile, Path unpackedDir, String name, String version, boolean snapshot, List<Daemon> daemons, List<Closeable> resources) {
        this.createdAt = System.currentTimeMillis();
        this.archiveFile = archiveFile;
        this.unpackedDir = unpackedDir;
        this.name = name;
        this.version = version;
        this.snapshot = snapshot;
        this.daemons = new EnumMap<>(InitType.class);
        this.resources = resources;
        
        // create set of daemons by init type
        daemons.stream().forEach((d) -> {
            Set<Daemon> s = this.daemons.get(d.getInitType());
            if (s == null) {
                s = new LinkedHashSet<>();
                this.daemons.put(d.getInitType(), s);
            }
            s.add(d);
        });
    }

    public long getCreatedAt() {
        return createdAt;
    }
    
    public Path getArchiveFile() {
        return archiveFile;
    }

    public Path getUnpackedDir() {
        return unpackedDir;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isSnapshot() {
        return snapshot;
    }
    
    public boolean hasDirectory(String dir) {
        return Files.exists(unpackedDir.resolve(dir));
    }
    
    public boolean hasDaemons() {
        return !daemons.isEmpty();
    }
    
    public boolean hasDaemons(InitType initType) {
        Set<Daemon> set = getDaemons(initType);
        if (set == null || set.isEmpty()) {
            return false;
        }
        return true;
    }
    
    public Map<InitType, Set<Daemon>> getDaemons() {
        return daemons;
    }

    public Set<Daemon> getDaemons(InitType initType) {
        // UPSTART -> SYSV
        if (initType == InitType.UPSTART) {
            initType = InitType.SYSV;
        }
        return daemons.get(initType);
    }

    public void verify() throws DeployerException {
        verifyDaemons();
    }
    
    private void verifyDaemons() throws DeployerException {
        // are there any daemons?
        if (this.daemons.isEmpty()) {
            return;
        }
        
        InitType refInitType = null;
        Set<Daemon> refDaemons = null;
        
        for (InitType initType : this.daemons.keySet()) {
            if (refDaemons == null) {
                refInitType = initType;
                refDaemons = this.daemons.get(initType);
            } else {
                Set<Daemon> verifyDaemons = this.daemons.get(initType);
                
                if (verifyDaemons.size() != refDaemons.size()) {
                    throw new DeployerException(
                        "Size mismatch between supported daemon init types: "
                        + refDaemons.size() + " " + refInitType + " daemons; "
                        + verifyDaemons.size() + " " + initType + " daemons!");
                }
                
                for (Daemon d : refDaemons) {
                    if (!verifyDaemons.contains(new Daemon(initType, d.getName(), null, null))) {
                        throw new DeployerException(
                            "Daemon missing: "
                            + "a " + refInitType + " daemon named " + d.getName() + " exists but is missing for init type " + initType);
                    }
                }
            }
        }
    }
    
    @Override
    public String toString() {
        if (snapshot) {
            return name + " v" + version + " (snapshot)";
        } else {
            return name + " v" + version;
        }
    }

    @Override
    public void close() throws IOException {
        // delete the unpacked dir and any other resources
        DeployHelper.deleteRecursively(this.unpackedDir);
        if (this.resources != null) {
            for (Closeable resource : resources) {
                resource.close();
            }
        }
    }
}