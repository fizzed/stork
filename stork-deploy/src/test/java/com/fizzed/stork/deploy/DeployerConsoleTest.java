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

import com.fizzed.blaze.core.Actions;
import com.fizzed.blaze.util.Streamables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class DeployerConsoleTest extends DeployerBaseTest {
    static private final Logger log = LoggerFactory.getLogger(DeployerConsoleTest.class);
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return TestHelper.getConsoleVagrantHosts();
    }
    
    public DeployerConsoleTest(String host) {
        super(host);
    }
    
    @Test
    public void deploy() throws Exception {
        Path assemblyFile = Paths.get("target/stork-console1-1.0.0-SNAPSHOT.tar.gz");
        
        DeployOptions options = new DeployOptions()
            .prefixDir("/opt")
            .user("vagrant")
            .group("vagrant");
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now (so we can access exec)
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.remove(true, "/opt");

        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, options, target);
        }

        // can we execute it
        String output
            = target.sshExec(false, false, "/opt/stork-console1/current/bin/stork-console1")
                .exitValues(0,1)
                .pipeOutput(Streamables.captureOutput())
                .runResult()
                .map(Actions::toCaptureOutput)
                .asString();
        
        // if hello world actually printed, awesome -- java is on the box
        // but if it isn't then we'll see if its the error at least
        assertThat(output, containsString("Hello World!"));
    }
    
    @Test
    public void deployWithDefaultOptions() throws Exception {
        Path assemblyFile = Paths.get("target/stork-console1-1.0.0-SNAPSHOT.tar.gz");
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now (so we can access exec)
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.remove(true, "/opt");

        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, new DeployOptions(), target);
        }

        // on freebsd and openbsd, the vagrant user is technically part of
        // the wheel group which means they can execute this app by default
        // for now we'll just verify it deployed
        List<BasicFile> listFiles = target.listFiles("/opt/stork-console1/current/");
        
        assertThat(listFiles, hasSize(2));
    }
    
    @Test
    public void deployWithUserThatDoesNotExist() throws Exception {
        Path assemblyFile = Paths.get("target/stork-console1-1.0.0-SNAPSHOT.tar.gz");

        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        DeployOptions options = new DeployOptions()
            .user("doesnotexist");

        try {
            try (Assembly assembly = Assemblys.process(assemblyFile)) {
                new Deployer().deploy(assembly, options, target);
            }
            fail("should have failed");
        } catch (DeployerException e) {
            assertThat(e.getMessage(), containsString("User 'doesnotexist' does not exist on target"));
        }
    }
    
    @Test
    public void deployWithGroupThatDoesNotExist() throws Exception {
        Path assemblyFile = Paths.get("target/stork-console1-1.0.0-SNAPSHOT.tar.gz");

        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        DeployOptions options = new DeployOptions()
            .group("doesnotexist");

        try {
            try (Assembly assembly = Assemblys.process(assemblyFile)) {
                new Deployer().deploy(assembly, options, target);
            }
            fail("should have failed");
        } catch (DeployerException e) {
            assertThat(e.getMessage(), containsString("Group 'doesnotexist' does not exist on target"));
        }
    }
    
    @Test
    public void deployWithRetain() throws Exception {
        Path assemblyFile = Paths.get("target/stork-console1-1.0.0-SNAPSHOT.tar.gz");
        
        DeployOptions options = new DeployOptions()
            .prefixDir("/opt")
            .user("vagrant")
            .group("vagrant")
            .retain(1);
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now (so we can access exec)
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.remove(true, "/opt");

        //
        // fresh install
        //
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            // fresh
            new Deployer().deploy(assembly, options, target);
        }
        
        List<Path> freshFiles = target.listFiles("/opt/stork-console1").stream()
            .map(bf -> bf.getPath())
            .collect(Collectors.toList());
        
        assertThat(freshFiles, hasSize(2));
        
        // find the versioned dir
        Path freshVersionedDir = freshFiles.stream()
            .filter(file -> !file.getFileName().toString().equals("current"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to find fresh versioned dir"));
        
        //
        // upgrade #1 install
        //
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            // upgrade
            new Deployer().deploy(assembly, options, target);
        }
        
        List<Path> upgrade1Files = target.listFiles("/opt/stork-console1").stream()
            .map(bf -> bf.getPath())
            .collect(Collectors.toList());
        
        assertThat(upgrade1Files, hasSize(3));

        Path updrade1VersionedDir = upgrade1Files.stream()
            .filter(file -> !file.getFileName().toString().equals("current"))
            .filter(file -> !file.equals(freshVersionedDir))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to find upgrade #1 versioned dir"));
        
        //
        // upgrade #2 install
        //
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            // upgrade againt (previous ver deleted)
            new Deployer().deploy(assembly, options, target);
        }
        
        List<Path> upgrade2Files = target.listFiles("/opt/stork-console1").stream()
            .map(bf -> bf.getPath())
            .collect(Collectors.toList());
        
        Path updrade2VersionedDir = upgrade2Files.stream()
            .filter(file -> !file.getFileName().toString().equals("current"))
            .filter(file -> !file.equals(freshVersionedDir))
            .filter(file -> !file.equals(updrade1VersionedDir))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to find upgrade #2 versioned dir"));
        
        // should be exactly 3 dirs
        assertThat(upgrade2Files, hasSize(3));
        
        // the fresh versioned dir should have been deleted
        assertThat(upgrade2Files, not(hasItem(freshVersionedDir)));
        assertThat(upgrade2Files, hasItem(updrade1VersionedDir));
        assertThat(upgrade2Files, hasItem(updrade2VersionedDir));
    }
}
