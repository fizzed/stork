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

import java.nio.file.Path;
import java.util.Set;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

public class AssemblysTest {
    
    @Test
    public void processConsoleAndDaemons() throws Exception {
        Path archiveFile = TestHelper.getResource("/fixtures/hello-world-1.2.4-SNAPSHOT.zip");
        
        try (Assembly assembly = Assemblys.process(archiveFile)) {
            assertThat(assembly.getName(), is("hello-world"));
            assertThat(assembly.getVersion(), is("1.2.4"));
            assertThat(assembly.isSnapshot(), is(true));
            assertThat(assembly.getArchiveFile(), is(archiveFile));
            assertThat(assembly.getUnpackedDir().getFileName().toString(), is("hello-world-1.2.4-SNAPSHOT"));
            
            Set<Daemon> sysvDaemons = assembly.getDaemons(InitType.SYSV);
            
            assertThat(sysvDaemons, hasSize(1));
            assertThat(sysvDaemons, contains(new Daemon(InitType.SYSV, "hello-daemon", null, null)));
            
            Set<Daemon> systemdDaemons = assembly.getDaemons(InitType.SYSTEMD);
            
            assertThat(systemdDaemons, hasSize(1));
            assertThat(systemdDaemons, contains(new Daemon(InitType.SYSTEMD, "hello-daemon", null, null)));
            
            // this should succeed
            assembly.verify();
            
            // remove a daemon type
            systemdDaemons.clear();
            
            try {
                assembly.verify();
                fail();
            } catch (DeployerException e) {
                assertThat(e.getMessage(), containsString("Size mismatch"));
            }
        }
    }
    
    @Test
    public void processConsole() throws Exception {
        Path archiveFile = TestHelper.getResource("/fixtures/hello-console-1.2.4.tar.gz");
        
        try (Assembly assembly = Assemblys.process(archiveFile)) {
            assertThat(assembly.getName(), is("hello-console"));
            assertThat(assembly.getVersion(), is("1.2.4"));
            assertThat(assembly.isSnapshot(), is(false));
            assertThat(assembly.getArchiveFile(), is(archiveFile));
            assertThat(assembly.getUnpackedDir().getFileName().toString(), is("hello-console-1.2.4"));
            assertThat(assembly.hasDaemons(), is(false));
        }
    }
    
}
