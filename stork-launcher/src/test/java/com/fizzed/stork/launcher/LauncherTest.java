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
package com.fizzed.stork.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.Systems;
import com.fizzed.blaze.ssh.SshExec;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.stork.test.LaunchData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Assume;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.BeforeClass;
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
    
    static private final Map<String,SshSession> sshs = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String host;
    private final Path exeCat;
    private final Path exeEchoConsole1;
    private final Path exeEchoConsole2;
    private final Path exeEchoConsole3;
    private final Path exeEchoDaemon1;
    private final SshSession ssh;
    static private boolean vagrantRsynced;
    
    public LauncherTest(String host) {
        this.host = host;
        this.exeCat = resolveExe("cat");
        this.exeEchoConsole1 = resolveExe("echo-console1");
        this.exeEchoConsole2 = resolveExe("echo-console2");
        this.exeEchoConsole3 = resolveExe("echo-console3");
        this.exeEchoDaemon1 = resolveExe("echo-daemon1");
        //this.symlinkJavaExe = resolveExe("symlink-java");
        if (!sshs.containsKey(host)) {
            sshs.put(host, sshConnect());
        }
        this.ssh = sshs.get(host);
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
    
    @BeforeClass
    static public void vagrantRsynced() throws Exception {
        if (!vagrantRsynced) {
            try {
                Systems.exec("vagrant", "rsync").exitValues(0, 1).run();
            } catch (Exception e) {
                // ignore
            }
            vagrantRsynced = true;
        }
    }
    
    @Before
    public void onlyIfHostIsRunning() throws Exception {
        assumeTrue("Is host running?", isHostRunning());
        
//        if (symlinkJava == null) {
//            // do not try to symlink java on a local windows client
//            if (isWindows()) {
//                return;  // skip
//            }
//            
//            String output = execute(0, symlinkJavaExe);
//            
//            // 3 lines
//            // /usr/lib/jvm/jdk1.8.0_77/jre
//            ///tmp/java-linked
//            ///tmp/java-linked with spaces
//            String[] lines = output.trim().split("\n");
//            symlinkJava = Paths.get(lines[1].trim());
//            symlinkJavaWithSpaces = Paths.get(lines[2].trim());
//        }
    }
    
    private boolean isLocal() {
        return host.equals("local");
    }
    
    private boolean isWindows() {
        return host.startsWith("windows")
            || (host.equals("local") && TestHelper.isWindows());
    }
    
    private Path resolveExe(String exeName) {
        if (isWindows()) {
            exeName += ".bat";
        }
        
        switch (this.host) {
            case "local":
                return Paths.get("target/stork/bin").resolve(exeName);
            default:
                return Paths.get("/vagrant/stork-launcher/target/stork/bin").resolve(exeName);
        }
    }
    
    public String execute(int exitValue, Path exe, String... args) throws Exception {
        return execute(exitValue, exe, null, args);
    }
    
    public String execute(int exitValue, Path exe, Map<String,String> environment, String... args) throws Exception {
        CaptureOutput captureOutput = Streamables.captureOutput();
        
        try {
            if (this.host.equals("local")) {
                Exec exec
                    = Systems.exec(exe)
                        .args((Object[]) args)
                        .exitValue(exitValue)
                        .pipeOutput(captureOutput)
                        .pipeError(captureOutput);
                
                if (environment != null) {
                    for (Map.Entry<String,String> entry : environment.entrySet()) {
                        exec.env(entry.getKey(), entry.getValue());
                    }
                }
                
                exec.run();
            } else {
                SshExec exec
                    = SecureShells.sshExec(ssh)
                        .command(exe)
                        .args((Object[]) args)
                        .exitValue(exitValue)
                        .pipeOutput(captureOutput)
                        .pipeError(captureOutput);
                
                if (environment != null) {
                    for (Map.Entry<String,String> entry : environment.entrySet()) {
                        exec.env(entry.getKey(), entry.getValue());
                    }
                }
                
                exec.run();
            }
        } catch (Exception e) {
            log.error("Unable to cleanly capture output: {}", captureOutput.asString());
            throw e;
        }
        
        return captureOutput.asString();
    }
    
    public String findJson(String json) {
        int start = json.indexOf("{");
        if (start < 0) {
            throw new IllegalArgumentException("Unable to find starting json {");
        }
        int end = json.lastIndexOf("}");
        if (end < 0) {
            throw new IllegalArgumentException("Unable to find ending json {");
        }
        return json.substring(start, end);
    }
    
    public <T> T readValue(Path file, Class<T> type) throws IOException {
        try {
            return objectMapper.readValue(file.toFile(), type);
        } catch (IOException e) {
            byte[] bytes = Files.readAllBytes(file);
            log.error("Unable to parse {} into json. File contained\n{}",
                file,
                new String(bytes, StandardCharsets.UTF_8));
            fail(e.getMessage());
            return null;
        }
    }
    
    public <T> T readValue(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            log.error("Unable to parse into json. json was\n{}", json);
            fail(e.getMessage());
            return null;
        }
    }
    
    @Test
    public void console() throws Exception {
        String json = execute(0, exeEchoConsole1);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
        
        // stork always sets launcher.name, launcher.type, launcher.app.dir
        assertThat(output.getSystemProperties(), hasEntry("launcher.name", "echo-console1"));
        assertThat(output.getSystemProperties(), hasEntry("launcher.type", "CONSOLE"));
        assertThat(output.getSystemProperties(), hasKey("launcher.app.dir"));

        // hard to determine real working dir so only do this on "local"
        if (isLocal()) {
            // verify working directory was retained
            Path ourWorkingDir = Paths.get((String)System.getProperty("user.dir"));
            Path appWorkingDir = Paths.get((String)output.getSystemProperties().get("user.dir"));
            Path appHomeDir = Paths.get((String)output.getSystemProperties().get("launcher.app.dir"));

            assertThat(appWorkingDir, is(ourWorkingDir));
            assertThat(appHomeDir, is(not(ourWorkingDir)));
            assertThat(appHomeDir, is(not(appWorkingDir)));
        }
    }
    
    @Test
    public void consoleWithArguments() throws Exception {
        String arg0 = "128401";
        String arg1 = "ahs3h1";
        
        String json = execute(0, exeEchoConsole1, arg0, arg1);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(2));
        assertThat(output.getArguments().get(0), is(arg0));
        assertThat(output.getArguments().get(1), is(arg1));
    }
    
    @Test
    public void consoleWithArgumentsThatHaveSpaces() throws Exception {
        // skip on windows since its just too complicated to escape them
        // correctly using a batch file
        Assume.assumeFalse(this.isWindows());
        
        String arg0 = "128 401";
        String arg1 = "ahs 3h1";
        
        String json = execute(0, exeEchoConsole1, arg0, arg1);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(2));
        assertThat(output.getArguments().get(0), is(arg0));
        assertThat(output.getArguments().get(1), is(arg1));
    }
    
    @Test
    public void consoleWithExtraArguments() throws Exception {
        // windows sshd does NOT allow env vars
        Assume.assumeFalse(this.host.startsWith("windows"));
        
        Map<String,String> environment = new HashMap<>();
        environment.put("EXTRA_APP_ARGS", "a b");
        
        String json = execute(0, exeEchoConsole1, environment);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(2));
        assertThat(output.getArguments().get(0), is("a"));
        assertThat(output.getArguments().get(1), is("b"));
    }
    
    @Test
    public void consoleWithExtraArgumentsThenCommandLineArguments() throws Exception {
        // windows sshd does NOT allow env vars
        Assume.assumeFalse(this.host.startsWith("windows"));
        
        Map<String,String> environment = new HashMap<>();
        environment.put("EXTRA_APP_ARGS", "a b");
        
        String json = execute(0, exeEchoConsole1, environment, "c", "d");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(4));
        assertThat(output.getArguments().get(0), is("a"));
        assertThat(output.getArguments().get(1), is("b"));
        assertThat(output.getArguments().get(2), is("c"));
        assertThat(output.getArguments().get(3), is("d"));
    }
    
    @Test
    public void consoleWithSystemProperties() throws Exception {
        String json = execute(0, exeEchoConsole1, "-Da=1", "-Db=2");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
        assertThat(output.getSystemProperties(), hasEntry("a", "1"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
    }
    
    @Test
    public void consoleWithArgumentsAndSystemProperties() throws Exception {
        String json = execute(0, exeEchoConsole1, "a", "-Da=1", "b", "-Db=2", "c");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(3));
        assertThat(output.getArguments().get(0), is("a"));
        assertThat(output.getArguments().get(1), is("b"));
        assertThat(output.getArguments().get(2), is("c"));
        assertThat(output.getSystemProperties(), hasEntry("a", "1"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
    }
    
    @Test
    public void consoleWithExtraJavaArgs() throws Exception {
        // windows sshd does NOT allow env vars
        Assume.assumeFalse(this.host.startsWith("windows"));
        
        Map<String,String> environment = new HashMap<>();
        environment.put("EXTRA_JAVA_ARGS", "-Da=1 -Db=2");
        
        String json = execute(0, exeEchoConsole1, environment);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
        assertThat(output.getSystemProperties(), hasEntry("a", "1"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
    }
    
    @Test
    public void consoleWithExtraJavaArgsAndCommandLineSystemProperties() throws Exception {
        // windows sshd does NOT allow env vars
        Assume.assumeFalse(this.host.startsWith("windows"));
        
        Map<String,String> environment = new HashMap<>();
        environment.put("EXTRA_JAVA_ARGS", "-Da=1 -Db=2");
        
        // this should override the "extra" one since it will be appended
        // after the extra_java_args would be
        String json = execute(0, exeEchoConsole1, environment, "-Da=2");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
        assertThat(output.getSystemProperties(), hasEntry("a", "2"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
    }
    
    @Test
    public void consoleMainClassNotFound() throws Exception {
        String output = execute(1, exeEchoConsole2);
        
        assertThat(output, containsString("Could not find or load main class com.fizzed.stork.test.ClassNotFoundMain"));
    }
    
    @Test
    public void consoleMinJavaVersionNotFound() throws Exception {
        String output = execute(1, exeEchoConsole3);

        // should this be printed out to error stream rather than stdout?
        assertThat(output, containsString("Unable to find Java runtime on system with version >= 1.10"));
    }
    
//    @Test
//    public void consoleJavaHomeWithSpaces() throws Exception {
//        assumeTrue("java symlink worked", symlinkJava != null);
//        
//        Map<String,String> environment = new HashMap<>();
//        //environment.put("PATH", "/bin:/usr/bin");
//        environment.put("JAVA_HOME", symlinkJavaWithSpaces.toString());
//        
//        String json = execute(0, exeEchoConsole1, environment);
//        
//        LaunchData output = this.readValue(json, LaunchData.class);
//        
//        assertThat(output.getConfirm(), is("Hello World!"));
//        assertThat(output.getArguments(), hasSize(0));
//    }
    
    @Test
    public void consoleJavaArgsWithSpaces() throws Exception {
        // windows sshd does NOT allow env vars
        Assume.assumeFalse(this.host.startsWith("windows"));
        
        Map<String,String> environment = new HashMap<>();
        environment.put("JAVA_ARGS", "-Da=1 -Db=2 -Dc=3");
        
        String json = execute(0, exeEchoConsole1, environment);
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getSystemProperties(), hasEntry("a", "1"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
        assertThat(output.getSystemProperties(), hasEntry("c", "3"));
    }
   
    @Test
    public void daemonRun() throws Exception {
        String json = execute(0, exeEchoDaemon1, "--run");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        
        // stork always sets launcher.name, launcher.type, launcher.app.dir
        // daemons that use the --run command are considered a CONSOLE app by stork
        assertThat(output.getSystemProperties(), hasEntry("launcher.name", "echo-daemon1"));
        assertThat(output.getSystemProperties(), hasEntry("launcher.type", "CONSOLE"));
        assertThat(output.getSystemProperties(), hasKey("launcher.app.dir"));
 
        // only do these on local since its hard to get correct path via ssh
        if (isLocal()) {
            // verify working directory was retained
            Path ourWorkingDir = Paths.get((String)System.getProperty("user.dir"));
            Path appWorkingDir = Paths.get((String)output.getSystemProperties().get("user.dir"));
            Path appHomeDir = Paths.get((String)output.getSystemProperties().get("launcher.app.dir"));

            assertThat(appWorkingDir, is(not((ourWorkingDir))));
            assertThat(appWorkingDir, is(appHomeDir));
        }
    }
    
    @Test
    public void daemonRunWithArguments() throws Exception {
        String json = execute(0, exeEchoDaemon1, "--run", "a", "b");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(2));
        assertThat(output.getArguments().get(0), is("a"));
        assertThat(output.getArguments().get(1), is("b"));
    }
    
    @Test
    public void daemonRunWithSystemProperties() throws Exception {
        String json = execute(0, exeEchoDaemon1, "--run", "-Da=1", "-Db=2");
        
        LaunchData output = this.readValue(json, LaunchData.class);
        
        assertThat(output.getConfirm(), is("Hello World!"));
        assertThat(output.getArguments(), hasSize(0));
        assertThat(output.getSystemProperties(), hasEntry("a", "1"));
        assertThat(output.getSystemProperties(), hasEntry("b", "2"));
    }
    
    @Test
    public void daemonStartRun() throws Exception {
        // do not run this test on windows
        assumeFalse(isWindows());
        
        // make sure the file does not exist between tests
        Path path = Paths.get("run/test-launch-data.json");
        Files.deleteIfExists(path);
        
        // by setting working dir mode to APP_HOME - this will be relative
        execute(0, exeEchoDaemon1, "--start-run", "--data-file", path.toString());

        //log.info("stdout: {}", stdout);

        Thread.sleep(1000L);
        
        String json = execute(0, exeCat, path.toString());

        LaunchData output = this.readValue(json, LaunchData.class);

        assertThat(output.getConfirm(), is("Hello World!"));

        // stork always sets launcher.name, launcher.type, launcher.app.dir
        // daemons that use the --run command are considered a CONSOLE app by stork
        assertThat(output.getSystemProperties(), hasEntry("launcher.name", "echo-daemon1"));
        assertThat(output.getSystemProperties(), hasEntry("launcher.type", "DAEMON"));
        assertThat(output.getSystemProperties(), hasKey("launcher.app.dir"));

        // only do these on local since its hard to get correct path via ssh
        if (isLocal()) {
            // verify working directory was retained
            Path ourWorkingDir = Paths.get((String)System.getProperty("user.dir"));
            Path appWorkingDir = Paths.get((String)output.getSystemProperties().get("user.dir"));
            Path appHomeDir = Paths.get((String)output.getSystemProperties().get("launcher.app.dir"));

            assertThat(appWorkingDir, is(not((ourWorkingDir))));
            assertThat(appWorkingDir, is(appHomeDir));
        }
    }
}
