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
import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.core.Actions;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.util.Streamables;
import java.io.IOException;
import static com.fizzed.blaze.SecureShells.sshExec;
import com.fizzed.blaze.ssh.SshSftpSession;
import com.fizzed.blaze.util.MutableUri;
//import com.fizzed.crux.vagrant.VagrantClient;
//import com.fizzed.crux.vagrant.VagrantClients;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Targets {
    static private final Logger log = LoggerFactory.getLogger(Targets.class);

//    static public final VagrantClient VAGRANT_CLIENT
//        = VagrantClients.cachingOrEmptyClient();
    
    static public Target connect(String uri) throws IOException {
        SshSession ssh = sshConnect(uri);
        SshSftpSession sftp = SecureShells.sshSftp(ssh).run();        
        return probe(ssh, sftp);
    }
    
    static public Target probe(SshSession ssh, SshSftpSession sftp) throws IOException {
        InitType initType = initType(ssh);
        
        Map<String,String> commands
            = which(ssh, Arrays.asList("doas", "sudo", "tar", "unzip"));
        
        String uname = uname(ssh);
        
        return new UnixTarget(ssh, sftp, uname, initType, "/tmp", commands);
    }
    
    static public SshSession sshConnect(String uri) throws IOException {
        MutableUri u = MutableUri.of(uri);
        
        if (u.getScheme() == null) {
            throw new IOException("uri missing scheme (not in format such as ssh://host)");
        }
        
        switch (u.getScheme()) {
            case "ssh":
            case "vagrant+ssh":
                return SecureShells.sshConnect(uri).run();
//            case "vagrant+ssh": {
//                String host = u.getHost();
//                log.info("Querying vagrant ssh-config for {}", host);
//                return SecureShells.sshConnect("ssh://" + host)
//                    .configFile(VAGRANT_CLIENT.sshConfig(host))
//                    .run();
//            }
            default:
                fail("Unsupported target uri. Support for either ssh://host or vagrant+ssh://host");
                return null;
        }
    }
    
    static private InitType initType(SshSession ssh) {
        String initTypeString
            =  sshExec(ssh)
                .command("sh").args("-c", 
                    "if \\$(/sbin/init --version 2>/dev/null | egrep -q upstart); then echo upstart; " +
                    "elif \\$(systemctl 2>/dev/null| egrep -q .mount); then echo systemd; " +
                    "elif [ -f /etc/init.d/cron ] || [ -f /etc/init.d/crond ]; then echo sysv; " +
                    "else echo unknown; fi")
                .runCaptureOutput(false)
                .asString();
        
        if (initTypeString == null) {
            return null;
        }
        
        switch (initTypeString.trim()) {
            case "sysv":
                return InitType.SYSV;
            case "systemd":
                return InitType.SYSTEMD;
            case "upstart":
                return InitType.UPSTART;
            default:
                return InitType.UNKNOWN;
        }
    }
    
    static private Map<String,String> which(SshSession ssh, List<String> commands) {
        // doesn't matter if we find it or not
        String whichString
            =  sshExec(ssh, "which", commands.toArray())
                .exitValues(0, 1, 2)
                .runCaptureOutput(false)
                .asString();

        if (whichString == null) {
            return Collections.emptyMap();
        }
        
        Map<String,String> result = new HashMap<>();
        
        String[] lines = whichString.split("\\\n");
        
        for (String line : lines) {
            Path cmd = Paths.get(line.trim());
            result.put(cmd.getFileName().toString(), line.trim());
        }
        
        return result;
    }
    
    static private String uname(SshSession ssh) {
        // doesn't matter if we find it or not
        return sshExec(ssh, "uname", "-srm")
            .exitValues(0)
            .runCaptureOutput(false)
            .asString()
            .trim();
    }
    
}
