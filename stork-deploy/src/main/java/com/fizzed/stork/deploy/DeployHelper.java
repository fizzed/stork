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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployHelper {
    private static final Logger log = LoggerFactory.getLogger(DeployHelper.class);
    
    static public void deleteRecursively(final Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        
        log.info("Deleting local {}", path);
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    if (!Files.isWritable(file)) {
                        log.error("File {} is not writable!!!", file);
                    }
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.error("Unable to delete {} wth attrs {}", file, attrs, e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException ioe) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException ioe) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        
        Files.deleteIfExists(path);
    }
    
    static private final DateTimeFormatter versionDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US);
    
    static public String toVersionDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return ldt.format(versionDateTimeFormatter);
    }
    
    static public String toFriendlyDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
}
