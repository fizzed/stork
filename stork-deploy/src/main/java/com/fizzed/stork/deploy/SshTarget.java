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
import com.fizzed.blaze.ssh.SshExec;
import com.fizzed.blaze.ssh.SshSession;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fizzed.blaze.ssh.SshSftpSession;
import java.io.IOException;
import java.util.Map;

abstract public class SshTarget extends Target {
    static private final Logger log = LoggerFactory.getLogger(SshTarget.class);

    protected final SshSession ssh;
    protected final SshSftpSession sftp;
    protected final Map<String,String> commands;

    public SshTarget(SshSession ssh, SshSftpSession sftp, String uname, InitType initType, String tempDir, Map<String,String> commands) {
        super(ssh.uri(), uname, initType, tempDir);
        this.ssh = ssh;
        this.sftp = sftp;
        this.commands = commands;
    }

    public SshExec sshExec(boolean sudo, boolean shell, Object... args) {
        Deque<Object> arguments = new ArrayDeque<>();

        if (sudo) {
            if (commands.containsKey("doas")) {
                arguments.add("doas");
            } else {
                arguments.add("sudo");
            }
        }

        if (shell) {
            arguments.add("sh");
            arguments.add("-c");
        }

        arguments.addAll(Arrays.asList(args));

        SshExec exec = SecureShells.sshExec(ssh);

        exec.command(arguments.pop().toString());

        while (arguments.size() > 0) {
            exec.arg(arguments.pop());
        }

        return exec;
    }
    
    @Override
    public void close() throws IOException {
        /**
        if (this.sftp != null) {
            this.sftp.close();
        }
        */
        if (this.ssh != null) {
            this.ssh.close();
        }
    }
    
}