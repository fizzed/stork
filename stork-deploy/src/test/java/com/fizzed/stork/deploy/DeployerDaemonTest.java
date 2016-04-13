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
import java.util.Collection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DeployerDaemonTest extends DeployerBaseTest {
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return TestHelper.getDaemonVagrantHosts();
    }
    
    public DeployerDaemonTest(String host) {
        super(host);
    }
    
    @Test
    public void deploy() throws Exception {
        Path assemblyFile = TestHelper.getResource("/fixtures/hello-world-1.2.4-SNAPSHOT.zip");
        
        DeployOptions options = new DeployOptions()
            .prefixDir("/opt")
            .user("daemon")
            .group("daemon");
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.sshExec(true, true, "kill $(ps aux | grep java | grep -v grep | awk \"{print \\$2}\")")
            .exitValues(0, 1, 2)
            .run();
        target.remove(true, "/opt/hello-world");
        target.remove(true, "/etc/init.d/hello-daemon");
        target.remove(true, "/etc/default/hello-daemon");
        target.remove(true, "/etc/sysconfig/hello-daemon");
        target.remove(true, "/etc/systemd/system/hello-daemon.service");
        
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, options, target);
        }

        // is the server running on port 8888?
        String output
            = target.sshExec(false, false, "curl", "http://localhost:8888")
                .exitValues(0)
                .pipeOutput(Streamables.captureOutput())
                .runResult()
                .map(Actions::toCaptureOutput)
                .asString();
        
        assertThat(output, containsString("Hi, i am an example daemon."));
        
        
        //
        // verify upgrade works too
        //
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, options, target);
        }

        // is the server running on port 8888?
        output
            = target.sshExec(false, false, "curl", "http://localhost:8888")
                .exitValues(0)
                .pipeOutput(Streamables.captureOutput())
                .runResult()
                .map(Actions::toCaptureOutput)
                .asString();
        
        assertThat(output, containsString("Hi, i am an example daemon.")); 
    }
    
}
