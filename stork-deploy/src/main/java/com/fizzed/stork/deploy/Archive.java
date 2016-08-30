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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Archive {
    static private final Logger log = LoggerFactory.getLogger(Archive.class);

    private final Path file;
    private final String format;
    
    public Archive(Path file) {
        this.file = file;
        this.format = format(file);
    }
    
    public Path getFile() {
        return this.file;
    }
    
    public String getName() {
        return this.file.getFileName().toString();
    }
    
    public String getNameWithNoExtension() {
        String name = getName();
        return name.substring(0, name.length() - this.format.length() - 1);
    }
    
    /**
     * Returns the format of the archive such as "tar.gz" or ".zip"
     * @return 
     */
    public String getFormat() {
        return this.format;
    }
    
    public Path unpack(Path unpackDir) throws IOException {
        Files.createDirectories(unpackDir);
        
        log.info("Unpacking {} to {}", file, unpackDir);
        
        // we need to know the top-level dir(s) created by unpack
        final Set<Path> firstLevelPaths = new LinkedHashSet<>();
        final AtomicInteger count = new AtomicInteger();
        
        try (ArchiveInputStream ais = newArchiveInputStream(file)) {
            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                try {
                    Path entryFile = Paths.get(entry.getName());
                    Path resolvedFile = unpackDir.resolve(entryFile);
                    
                    firstLevelPaths.add(entryFile.getName(0));
                    
                    log.debug("{}", resolvedFile);
                    
                    if (entry.isDirectory()) {
                        Files.createDirectories(resolvedFile);
                    } else {
                        unpackEntry(ais, resolvedFile);
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
    
    
    static public String format(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".tar.gz")) {
            return "tar.gz";
        } else if (name.endsWith(".zip")) {
            return "zip";
        } else {
            return null;
        }
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
    
    static private void unpackEntry(ArchiveInputStream ais, Path target) throws IOException {   
        // always make sure parent dir exists
        Files.createDirectories(target.getParent());
        
        try (OutputStream os = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                int len = 0;
                byte[] BUFFER = new byte[1024];
                while ((len = ais.read(BUFFER)) != -1) {
                    bos.write(BUFFER, 0, len);
                }
            }
        }
    }
    
    static public Archive pack(Path unpackedDir, String format) throws IOException {
        String name = unpackedDir.getFileName().toString();
        Path archiveFile = unpackedDir.resolveSibling(name + "." + format);
        return pack(unpackedDir, archiveFile, format);
    }
    
    static public Archive pack(Path unpackedDir, Path archiveFile, String format) throws IOException {
        log.info("Packing {} to {}", unpackedDir, archiveFile);
        
        try (ArchiveOutputStream aos = newArchiveOutputStream(archiveFile, format)) {
            packEntry(aos, unpackedDir, unpackedDir.getFileName().toString(), false);
        }
        
        return new Archive(archiveFile);
    }
    
    static private ArchiveOutputStream newArchiveOutputStream(Path file, String format) throws IOException {
        switch (format) {
            case "tar.gz":
                TarArchiveOutputStream tgzout = new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(file)));
                tgzout.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
                tgzout.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                return tgzout;
            case "zip":
                return new ZipArchiveOutputStream(Files.newOutputStream(file));
            default:
                throw new IOException("Unsupported archive file type (we support .tar.gz and .zip)");
        }
    }
    
    static public void packEntry(ArchiveOutputStream aos, Path dirOrFile, String base, boolean appendName) throws IOException {
        String entryName = base;
        
        if (appendName) {
            if (!entryName.equals("")) {
                if (!entryName.endsWith("/")) {
                    entryName += "/" + dirOrFile.getFileName();
                } else {
                    entryName += dirOrFile.getFileName();
                }
            } else {
                entryName += dirOrFile.getFileName();
            }
        }
        
        ArchiveEntry entry = aos.createArchiveEntry(dirOrFile.toFile(), entryName);

        if (Files.isRegularFile(dirOrFile)) {
            if (entry instanceof TarArchiveEntry && Files.isExecutable(dirOrFile)) {
                // -rwxr-xr-x
                ((TarArchiveEntry)entry).setMode(493);
            } else {
                // keep default mode
            }
        }

        aos.putArchiveEntry(entry);

        if (Files.isRegularFile(dirOrFile)) {
            Files.copy(dirOrFile, aos);
            aos.closeArchiveEntry();
        } else {
            aos.closeArchiveEntry();
            List<Path> children = Files.list(dirOrFile).collect(Collectors.toList());
            if (children != null){
                for (Path childFile : children) {
                    packEntry(aos, childFile, entryName + "/", true);
                }
            }
        }
    }
    
}
