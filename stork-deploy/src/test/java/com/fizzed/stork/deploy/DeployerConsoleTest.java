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
import java.util.List;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class DeployerConsoleTest extends DeployerBaseTest {
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return TestHelper.getAllVagrantHosts();
    }
    
    public DeployerConsoleTest(String host) {
        super(host);
    }
    
    @Test
    public void deploy() throws Exception {
        Path assemblyFile = TestHelper.getResource("/fixtures/hello-console-1.2.4.tar.gz");
        
        Options options = new Options()
            .prefixDir("/opt")
            .user("vagrant")
            .group("vagrant");
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now (so we can access exec)
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.remove(true, "/opt/hello-console");

        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, options, target);
        }

        // can we execute it
        String output
            = target.sshExec(false, false, "/opt/hello-console/current/bin/hello-console")
                .exitValues(0,1)
                .pipeOutput(Streamables.captureOutput())
                .runResult()
                .map(Actions::toCaptureOutput)
                .asString();
        
        // if hello world actually printed, awesome -- java is on the box
        // but if it isn't then we'll see if its the error at least
        assertThat(output, anyOf(
            containsString("Unable to find Java runtime"),
            containsString("Hello World!")));
    }
    
    @Test
    public void deployWithNoUserOrGroup() throws Exception {
        Path assemblyFile = TestHelper.getResource("/fixtures/hello-console-1.2.4.tar.gz");
        
        // create our own target for assisting with preparing for tests
        // assume its unix for now (so we can access exec)
        UnixTarget target = (UnixTarget)Targets.connect(getHostUri());

        // make sure app does not exist on host
        target.remove(true, "/opt/hello-console");

        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, new Options(), target);
        }

        // on freebsd and openbsd, the vagrant user is technically part of
        // the wheel group which means they can execute this app by default
        // for now we'll just verify it deployed
        List<BasicFile> listFiles = target.listFiles("/opt/hello-console/current/");
        
        assertThat(listFiles, hasSize(2));
        
        /**
        CaptureOutput capture = Streamables.captureOutput();
        try {
        // we shouldn't be able to execute it since it was installed with sudo
        target.sshExec(false, false, "/opt/hello-console/current/bin/hello-console")
        .exitValues(0,1)
        .pipeOutput(capture)
        .pipeError(capture)
        .runResult();
        fail(capture.asString());
        } catch (UnexpectedExitValueException e) {
        // expected permission denied
        assertThat(capture.asString(), containsString("denied"));
        }
         */

    }
    
}
