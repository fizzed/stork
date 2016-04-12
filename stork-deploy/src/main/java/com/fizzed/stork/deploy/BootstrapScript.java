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

import static com.fizzed.blaze.Contexts.fail;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Written as a blaze script.
 */
public class BootstrapScript {
    
    private final Path assemblyFile;
    private final List<String> targets;
    
    public BootstrapScript(Path assemblyFile, List<String> targets) {
        this.assemblyFile = assemblyFile;
        this.targets = targets;
    }
    
    private void verifyTargets() {
        if (this.targets == null || this.targets.isEmpty()) {
            fail("--target <target[,target,...]> arguments are required");
        }
    }
    
    private void verifyAssemblyFile() {
        if (this.assemblyFile == null) {
            fail("--assembly <file> arguments are required");
        }
    }
    
    public void verify() throws DeployerException, IOException {
        verifyAssemblyFile();
        verifyTargets();
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            for (String uri : targets) {
                new Deployer().verify(assembly, new Options(), uri);
            }
        }
    }
    
    public void deploy() throws DeployerException, IOException {
        verifyAssemblyFile();
        verifyTargets();
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            for (String uri : targets) {
                new Deployer().deploy(assembly, new Options(), uri);
            }
        }
    }
    
}
