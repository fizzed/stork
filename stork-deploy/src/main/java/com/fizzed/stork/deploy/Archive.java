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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Archive {
    static private final Logger log = LoggerFactory.getLogger(Archive.class);

    private final Path file;

    public Archive(Path file) {
        this.file = file;
    }
    
    public Path unpack(Path unpackDir) throws IOException {
        Files.createDirectories(unpackDir);
        
        log.info("Unpacking {} to {}", file, unpackDir);
        
        // we need to know the top-level dir(s) created by unpack
        final Set<Path> firstLevelPaths = new LinkedHashSet<>();
        final AtomicInteger count = new AtomicInteger();
        
        try (ArchiveInputStream ais = newArchiveInputStream(file)) {
            ArchiveEntry entry = null;
            
            while ((entry = ais.getNextEntry()) != null) {
                try {
                    Path entryFile = Paths.get(entry.getName());
                    Path resolvedFile = unpackDir.resolve(entryFile);
                    
                    firstLevelPaths.add(entryFile.getName(0));
                    
                    log.debug("{}", resolvedFile);
                    
                    if (entry.isDirectory()) {
                        Files.createDirectories(resolvedFile);
                    } else {
                        extractEntry(ais, resolvedFile);
                        count.incrementAndGet();
                    }
                } catch (IOException | IllegalStateException | IllegalArgumentException e) {
                    log.error("", e);
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (firstLevelPaths.size() != 1) {
            throw new IOException("Only archives with a single top-level directory are supported!");
        }
        
        Path assemblyDir = unpackDir.resolve(firstLevelPaths.iterator().next());
        
        log.info("Unpacked {} files to {}", count, assemblyDir);
        
        return assemblyDir;
    }
    
    static private ArchiveInputStream newArchiveInputStream(Path file) throws IOException {
        String name = file.getFileName().toString();
        
        if (name.endsWith(".tar.gz")) {
            return new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(file), true));
        } else if (name.endsWith(".zip")) {
            return new ZipArchiveInputStream(Files.newInputStream(file));
        } else {
            throw new IOException("Unsupported archive file type (we support .tar.gz and .zip)");
        }
    }
    
    static private void extractEntry(ArchiveInputStream ais, Path target) throws IOException {   
        // always make sure parent dir exists
        Files.createDirectories(target.getParent());
        
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            int len = 0;
            byte[] BUFFER = new byte[1024];
            
            while ((len = ais.read(BUFFER)) != -1) {
                bos.write(BUFFER, 0, len);
            }
        }
    }
    
}
