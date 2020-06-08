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

import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftpSession;
import com.fizzed.blaze.util.MutableUri;
import static com.fizzed.stork.deploy.DeployerBaseTest.VAGRANT_CLIENT;
import java.nio.file.Path;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.junit.Assume.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TargetsTest {

    private final String host;
    private final MutableUri uri;
    private final Path sshConfigFile;
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return TestHelper.getConsoleVagrantHosts();
    }
    
    @Before
    public void isVagrantHostRunning() {
        assumeTrue("is " + host + " running?", VAGRANT_CLIENT.machinesRunning().contains(host));
    }
    
    public TargetsTest(String host) {
        this.host = host;
        this.uri = MutableUri.of("ssh://{}", host);
        ContextHolder.set(new ContextImpl(null, null, null, null));
        if (VAGRANT_CLIENT.machinesRunning().contains(host)) {
            this.sshConfigFile = VAGRANT_CLIENT.sshConfig(host);
        } else {
            this.sshConfigFile = null;
        }
    }
    
    @Test
    public void probe() throws Exception {
        try (SshSession ssh = SecureShells.sshConnect(uri).configFile(sshConfigFile).run()) {
            try (SshSftpSession sftp = SecureShells.sshSftp(ssh).run()) {
                Target target = Targets.probe(ssh, sftp);

                switch (host) {
                    case "ubuntu14":
                        assertThat(target.getInitType(), is(InitType.UPSTART));
                        assertThat(target, instanceOf(UnixTarget.class));
                        break;
                    case "ubuntu16":
                    case "debian8":
                    case "centos7":
                        assertThat(target.getInitType(), is(InitType.SYSTEMD));
                        assertThat(target, instanceOf(UnixTarget.class));
                        break;
                    case "freebsd10":
                        assertThat(target.getInitType(), is(InitType.UNKNOWN));
                        assertThat(target, instanceOf(UnixTarget.class));
                        break;
                    case "openbsd58":
                        assertThat(target.getInitType(), is(InitType.UNKNOWN));
                        assertThat(target, instanceOf(UnixTarget.class));
                        break;
                    default:
                        fail("Did you forget to add host " + host + "?");
                        break;
                }
            }
        }
    }
    
}
