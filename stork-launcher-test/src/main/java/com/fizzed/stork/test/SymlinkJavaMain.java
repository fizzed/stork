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
package com.fizzed.stork.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SymlinkJavaMain {
 
    static public void main(String[] args) throws Exception {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path javaHomeDir = Paths.get(System.getProperty("java.home"));
        System.out.println(javaHomeDir);
        
        Path javaLinkedDir = tempDir.resolve("java-linked");
        Files.deleteIfExists(javaLinkedDir);
        Files.createSymbolicLink(javaLinkedDir, javaHomeDir);
        System.out.println(javaLinkedDir);
        
        Path javaLinkedWithSpacesDir = tempDir.resolve("java with spaces");
        Files.deleteIfExists(javaLinkedWithSpacesDir);
        Files.createSymbolicLink(javaLinkedWithSpacesDir, javaHomeDir);
        System.out.println(javaLinkedWithSpacesDir);
    }
}
