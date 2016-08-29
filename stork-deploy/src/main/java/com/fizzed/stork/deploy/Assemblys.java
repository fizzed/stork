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

import com.fizzed.blaze.util.Globber;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Assemblys {
    static private final Logger log = LoggerFactory.getLogger(Assemblys.class);
    
    static public Assembly process(Path archiveFile) throws IOException {
        Path tempDir = Files.createTempDirectory("stork-deploy.");
        CloseablePath closeablePath = new CloseablePath(tempDir);
        return process(archiveFile, tempDir, Arrays.asList(closeablePath));
    }
    
    static public Assembly process(Path archiveFile, Path workDir, List<Closeable> resources) throws IOException {
        if (!Files.exists(archiveFile)) {
            throw new IOException("Archive file " + archiveFile + " does not exist");
        }
        
        Archive archive = new Archive(archiveFile);
        
        Path unpackedDir = archive.unpack(workDir);
        
        String filename = unpackedDir.getFileName().toString();
        
        boolean snapshot = false;
        if (filename.endsWith("-SNAPSHOT")) {
            snapshot = true;
            filename = filename.replace("-SNAPSHOT", "");
        }
        
        // find version and strip it out
        int versionPos = filename.lastIndexOf("-");
        if (versionPos < 0) {
            throw new IOException("Unable to parse version from " + filename);
        }
        
        String version = filename.substring(versionPos+1);
        String name = filename.substring(0, versionPos);
        
        // find all supported daemon init types
        List<Daemon> daemons = new ArrayList<>();
        
        findSysvDaemons(unpackedDir, daemons);
        findSystemdDaemons(unpackedDir, daemons);
        
        return new Assembly(archive, unpackedDir, name, version, snapshot, daemons, resources);
    }
    
    static private void findSysvDaemons(Path unpackedDir, List<Daemon> daemons) throws IOException {
        Path dir = unpackedDir.resolve("share/init.d");
        
        if (!Files.exists(dir)) {
            log.debug("{} dir not found (no sysv daemons?)", dir);
            return;
        }
        
        Globber.globber(dir, "*.init").filesOnly().stream().forEach((initFile) -> {
            log.debug("Detected sysv daemon {}", initFile);
            String name = initFile.getFileName().toString().replace(".init", "");
            daemons.add(new Daemon(InitType.SYSV, name, null, null));
        });
    }
    
    static private void findSystemdDaemons(Path unpackedDir, List<Daemon> daemons) throws IOException {
        Path dir = unpackedDir.resolve("share/systemd");
        
        if (!Files.exists(dir)) {
            log.debug("{} dir not found (no systemd daemons?)", dir);
            return;
        }
        
        Globber.globber(dir, "*.service").filesOnly().stream().forEach((serviceFile) -> {
            log.debug("Detected systemd daemon {}", serviceFile);
            String name = serviceFile.getFileName().toString().replace(".service", "");
            daemons.add(new Daemon(InitType.SYSTEMD, name, null, null));
        });
    }
    
}
