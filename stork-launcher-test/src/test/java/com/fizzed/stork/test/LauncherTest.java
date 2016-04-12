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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.Systems;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class LauncherTest {
    static private final Logger log = LoggerFactory.getLogger(LauncherTest.class);
    
    @Parameters(name = "{index}: host={0}")
    public static Collection<String> data() {
        return TestHelper.hosts();
    }
    
    @Before
    public void onlyIfHostIsRunning() {
        assumeTrue("Is host running?", isHostRunning());
    }
    
    private final String host;
    private final Path exeHello1;
    private final Path exeHello2;
    private final Path exeHello3;
    private final SshSession ssh;
    
    public LauncherTest(String host) {
        this.host = host;
        this.exeHello1 = resolveExe("hello1");
        this.exeHello2 = resolveExe("hello2");
        this.exeHello3 = resolveExe("hello3");
        this.ssh = sshConnect();
    }
    
    private boolean isHostRunning() {
        return host.equals("local")
            || TestHelper.VAGRANT_CLIENT.machinesRunning().contains(host);
    }
    
    private SshSession sshConnect() {
        switch (this.host) {
            case "local":
                return null;
            default:
                if (isHostRunning()) {
                    Path sshConfig = TestHelper.VAGRANT_CLIENT.sshConfig(host);
                    return SecureShells.sshConnect("ssh://" + host).configFile(sshConfig).run();
                } else {
                    return null;
                }
        }
    }
    
    private Path resolveExe(String exeName) {
        switch (this.host) {
            case "local":
                if (TestHelper.isWindows()) {
                    return Paths.get("target/stork/bin").resolve(exeName + ".bat");
                } else {
                    return Paths.get("target/stork/bin").resolve(exeName);
                }
            default:
                return Paths.get("/vagrant/stork-launcher-test/target/stork/bin").resolve(exeName);
        }
    }
    
    public String execute(int exitValue, Path exe, String... args) throws Exception {
        CaptureOutput captureOutput = Streamables.captureOutput();
        
        try {
            if (this.host.equals("local")) {
                Systems.exec(exe)
                   .args((Object[]) args)
                   .exitValue(exitValue)
                   .pipeOutput(captureOutput)
                   .pipeError(captureOutput)
                   .run();
            } else {
                SecureShells.sshExec(ssh)
                   .command(exe)
                   .args((Object[]) args)
                   .exitValue(exitValue)
                   .pipeOutput(captureOutput)
                   .pipeError(captureOutput)
                   .run();
            }
        } catch (Exception e) {
            log.error("Unable to cleanly capture output: {}", captureOutput.asString());
            throw e;
        }
        
        return captureOutput.asString();
    }
    
    @Test
    public void basic() throws Exception {
        String json = execute(0, exeHello1);
        
        HelloOutput output = new ObjectMapper().readValue(json, HelloOutput.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
    }
    
    @Test
    public void argumentsPassedThrough() throws Exception {
        String arg0 = "128401";
        String arg1 = "ahs3h1";
        
        String json = execute(0, exeHello1, arg0, arg1);
        
        HelloOutput output = new ObjectMapper().readValue(json, HelloOutput.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(2));
        assertThat(output.getArguments().get(0), is(arg0));
        assertThat(output.getArguments().get(1), is(arg1));
    }
    
    @Test
    public void minJavaVersionNotFound() throws Exception {
        String output = execute(1, exeHello2);

        // should this be printed out to error stream rather than stdout?
        assertThat(output, containsString("Unable to find Java runtime on system with version >= 1.10"));
    }
    
    @Test
    public void mainClassNotFound() throws Exception {
        String output = execute(1, exeHello3);
        
        assertThat(output, containsString("Could not find or load main class com.fizzed.stork.test.ClassNotFoundMain"));
    }
}
