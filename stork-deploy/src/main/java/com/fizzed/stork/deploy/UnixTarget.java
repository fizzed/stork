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
import com.fizzed.blaze.core.Actions;
import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.ssh.SshExec;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.util.Streamables;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fizzed.blaze.ssh.SshFile;
import com.fizzed.blaze.ssh.SshFileAttributes;
import com.fizzed.blaze.ssh.SshSftpNoSuchFileException;
import com.fizzed.blaze.ssh.SshSftpSession;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnixTarget extends SshTarget {
    static private final Logger log = LoggerFactory.getLogger(UnixTarget.class);

    public UnixTarget(SshSession ssh, SshSftpSession sftp, String uname, InitType initType, String tempDir, Map<String,String> commands) {
        super(ssh, sftp, uname, initType, tempDir, commands);
    }
    
    @Override
    public void put(Path source, String target) {
        sftp.put()
            .source(source)
            .target(target)
            .run();
    }

    @Override
    public List<BasicFile> listFiles(Object path) {
        try {
            List<SshFile> files = sftp.ls(path.toString());
            List<BasicFile> basicFiles = new ArrayList<>(files.size());
            files.stream().forEach((file) -> {
                SshFileAttributes attrs = file.attributes();
                long createdAt = attrs.creationTime().toMillis();
                basicFiles.add(new BasicFile(file.path(), createdAt, attrs.uid(), attrs.gid()));
            });
            return basicFiles;
        } catch (SshSftpNoSuchFileException e) {
            return null;
        }
    }
    
    @Override
    public Path readlink(Object path) {
        try {
            return sftp.readlink(path.toString());
        } catch (SshSftpNoSuchFileException e) {
            return null;
        }
    }
    
    @Override
    public Path realpath(Object path) {
        try {
            return sftp.realpath(path.toString());
        } catch (SshSftpNoSuchFileException e) {
            return null;
        }
    }
    
    @Override
    public void chown(boolean sudo, boolean recursive, String owner, String target) {
        String options = "";
        if (recursive) {
            options = "-R";
        }

        sshExec(sudo, false, "chown", options, owner, target).run();

        log.info("Set owner to {} for {}", owner, target);
    }

    @Override
    public void chmod(boolean sudo, boolean recursive, String permissions, String target) {
        String options = "";
        if (recursive) {
            options = "-R";
        }

        sshExec(sudo, false, "chmod", options, permissions, target).run();

        log.info("Set perms to {} for {}", permissions, target);
    }

    @Override
    public void symlink(boolean sudo, String target, String link) {
        remove(sudo, link);
        sshExec(sudo, false, "ln", "-s", target, link).run();
        log.info("Symlink " + link + " -> " + target);
    }

    @Override
    public void copyFiles(boolean sudo, String source, String target) {
        sshExec(sudo, true, "if [ -e " + source + " ]; then cp -R " + source + " " + target + "; fi").run();
        log.info("Copied file(s) from " + source + " to " + target);
    }

    @Override
    public void moveFiles(boolean sudo, String source, String target) {
        sshExec(sudo, true, "if [ -d " + source + " ]; then mv " + source + " " + target + "; fi").run();
        log.info("Moved file(s) from " + source + " to " + target);
    }

    @Override
    public void createDirectories(boolean sudo, Object path) {
        sshExec(sudo, false, "mkdir", "-p", path).run();
        log.info("Created dir(s) {}", path);
    }

    @Override
    public void remove(boolean sudo, Object... paths) {
        // remove any old assembly or unpacked version
        SshExec sshExec = sshExec(sudo, false, "rm", "-Rf");
        for (Object path : paths) {
            sshExec.arg(path);
        }
        sshExec.run();
        log.info("Removed {}", (Object) paths);
    }

    @Override
    public void unpack(String path, String targetDir) {
        if (path.endsWith(".tar.gz")) {
            sshExec(false, false, "tar", "xzf", path, "-C", targetDir).run();
        } else if (path.endsWith(".zip")) {
            sshExec(false, false, "unzip", "-q", "-d", targetDir, path).run();
        } else {
            fail("Unable to support file extension for '" + path + "'");
        }
        log.info("Unpacked {}", path);
    }

    public Integer getUserId(String user) {
        try {
            String userId
                = sshExec(false, false, "id", "-u", user)
                    .pipeOutput(Streamables.captureOutput())
                    .pipeError(Streamables.nullOutput())
                    .runResult()
                    .map(Actions::toCaptureOutput)
                    .asString()
                    .trim();
            return Integer.valueOf(userId);
        } catch (UnexpectedExitValueException e) {
            return null;
        }
    }

    @Override
    public boolean hasUser(String user) {
        return getUserId(user) != null;
    }

    public Integer getGroupId(String group) {
        try {
            String groupId
                = sshExec(false, false, "id", "-g", group)
                    .pipeOutput(Streamables.captureOutput())
                    .pipeError(Streamables.nullOutput())
                    .runResult()
                    .map(Actions::toCaptureOutput)
                    .asString()
                    .trim();
            return Integer.valueOf(groupId);
        } catch (UnexpectedExitValueException e) {
            return null;
        }
    }

    @Override
    public boolean hasGroup(String group) {
        return getGroupId(group) != null;
    }
    
    @Override
    public void stopDaemon(Daemon daemon) throws DeployerException {
        switch (this.getInitType()) {
            case SYSV:
            case UPSTART:
                try {
                    log.info("Trying to stop daemon {}...", daemon.getName());
                    sshExec(true, false, "service", daemon.getName(), "stop").run();
                } catch (UnexpectedExitValueException e) {
                    throw new DeployerException(
                        "Unable to stop service " + daemon.getName()
                        + ". Exit value " + e.getActual() + ". Output from failed command is above.");
                }
                break;
            case SYSTEMD:
                try {
                    // 0 = success, 5 = service not loaded
                    log.info("Trying to stop daemon {}...", daemon.getName());
                    sshExec(true, false, "systemctl", "stop", daemon.getName())
                        .exitValues(0, 5)
                        .run();
                    // TODO: should we do our own loop to confirm it stopped?
                } catch (UnexpectedExitValueException e) {
                    throw new DeployerException(
                        "Unable to stop service " + daemon.getName()
                        + ". Exit value " + e.getActual() + ". Output from failed command is above.");
                }
                break;
            default:
                throw new DeployerException("Unable to support init type " + getInitType());
        }
    }

    @Override
    public void startDaemon(Daemon daemon)  throws DeployerException {
        switch (this.getInitType()) {
            case SYSV:
            case UPSTART:
                try {
                    log.info("Trying to start daemon {}...", daemon.getName());
                    sshExec(true, false, "service", daemon.getName(), "start").run();
                    log.info("Daemon {} started!", daemon.getName());
                } catch (UnexpectedExitValueException e) {
                    throw new DeployerException(
                        "Unable to start service " + daemon.getName()
                        + ". Exit value " + e.getActual() + ". Output from failed command is above.");
                }
                break;
            case SYSTEMD:
                try {
                    log.info("Reloading systemd daemon...", daemon.getName());
                    sshExec(true, false, "systemctl", "daemon-reload").run();

                    log.info("Trying to start daemon {}...", daemon.getName());
                    sshExec(true, false, "systemctl", "start", daemon.getName()).run();

                    // wait for service to truly start
                    long timeout = 15000L;
                    long now = System.currentTimeMillis();
                    boolean confirmed = false;
                    
                    while (System.currentTimeMillis() < (now+timeout)) {
                        log.info("Querying systemctl to verify {} started...", daemon.getName());
                        
                        // run a status command so user can see what's up
                        String output
                            = sshExec(true, false, "systemctl", "status", daemon.getName())
                                .pipeOutput(Streamables.captureOutput())
                                .runResult()
                                .map(Actions::toCaptureOutput)
                                .asString();
                        
                        // TODO: this needs to be configurable!
                        if (output.contains("OK")) {
                            confirmed = true;
                            break;
                        }
                        
                        Thread.sleep(1000L);
                    }
                    
                    if (!confirmed) {
                        sshExec(true, false, "systemctl", "status", daemon.getName()).run();
                        throw new DeployerException("Unable to confirm service started within " + timeout + " ms");
                    } else {
                        log.info("Daemon {} started!", daemon.getName());
                    }
                } catch (UnexpectedExitValueException e) {
                    // run a status command so user can see what's up
                    sshExec(true, false, "systemctl", "status", daemon.getName()).run();
                    throw new DeployerException(
                        "Unable to start service " + daemon.getName()
                        + ". Exit value " + e.getActual() + ". Output from failed command is above.");
                } catch (InterruptedException e) {
                    throw new DeployerException(e.getMessage());
                }
                break;
            default:
                throw new DeployerException("Unable to support init type " + getInitType());
        }
    }

    @Override
    public void installDaemon(Deployment install, Daemon daemon, boolean onBoot) throws DeployerException {
        switch (this.getInitType()) {
            case SYSV:
            case UPSTART:
                installSysvDaemon(install, daemon, onBoot);
                break;
            case SYSTEMD:
                installSystemdDaemon(install, daemon, onBoot);
                break;
            default:
                throw new DeployerException("Unable to support init type " + getInitType());
        }
    }
    
    private void installDaemonDefaults(Deployment install, Daemon daemon) {
        // configure service config files
        for (String dir : Arrays.asList("default", "sysconfig")) {
            String defaultsFile = "/etc/" + dir + "/" + daemon.getName();

            String cmd
                = "if [ -d /etc/" + dir + " ]; then "
                + "  if [ ! -f " + defaultsFile + " ]; then "    
                + "    echo \"APP_HOME=\\\"" + install.getCurrentDir() + "\\\"\" > " + defaultsFile + "; "
                + "    echo \"APP_USER=\\\"" + install.getUser().orElse("") + "\\\"\" >> " + defaultsFile + "; "
                + "    echo \"APP_GROUP=\\\"" + install.getGroup().orElse("") + "\\\"\" >> " + defaultsFile + "; "
                + "    exit 20; "
                + "  else "
                + "    exit 30; "
                + "  fi "
                + "else "
                + "  exit 10; "
                + "fi";

            Integer exitValue
                = sshExec(true, true, cmd)
                    .exitValues(10, 20, 30)
                    .run();

            if (exitValue == 20) {
                log.info("Created {}", defaultsFile);
                break; // no need to keep running loop
            } else if (exitValue == 30) {
                log.info("Defaults {} already exists (will not overwrite)", defaultsFile);
                break; // no need to keep running loop
            }
        }
    }
    
    private void installSysvDaemon(Deployment install, Daemon daemon, boolean onBoot) {
        // symlink to init.d
        String initdFile = "/etc/init.d/" + daemon.getName();
        String initFile = install.getCurrentDir() + "/share/init.d/" + daemon.getName() + ".init";
        symlink(true, initFile, initdFile);

        installDaemonDefaults(install, daemon);

        // auto start?
        if (onBoot) {
            String cmd
                = "if type \"chkconfig\" > /dev/null; then "
                + "  chkconfig --add " + daemon.getName() + "; "
                + "  exit 1; "
                + "elif type \"update-rc.d\" > /dev/null; then "
                + "  update-rc.d " + daemon.getName() + " defaults; "
                + "  exit 2; "
                + "else "
                + "  exit 3; "
                + "fi";

            Integer exitValue
                = sshExec(true, true, cmd)
                    .exitValues(1, 2, 3)
                    .run();

            switch (exitValue) {
                case 1:
                    log.info("Daemon {} will start at boot (via chkconfig)", daemon.getName());
                    break;
                case 2:
                    log.info("Daemon {} will start at boot (via update-rc.d)", daemon.getName());
                    break;
                case 3:
                    log.error("Daemon {} will be unable to start at boot (neither chkconfig or update-rc.d found)", daemon.getName());
                    break;
            }
        }
    }
    
    private void installSystemdDaemon(Deployment install, Daemon daemon, boolean onBoot) {
        // TODO: create new daemon file locally, then upload, and install?
        
        // copy over service file or symlink it?
        String serviceFile = "/etc/systemd/system/" + daemon.getName() + ".service";
        String sourceServiceFile = install.getCurrentDir() + "/share/systemd/" + daemon.getName() + ".service";
        copyFiles(true, sourceServiceFile, serviceFile);

        installDaemonDefaults(install, daemon);

        /**
        // auto start?
        if (onBoot) {
            String cmd
                = "if type \"chkconfig\" > /dev/null; then "
                + "  chkconfig --add " + daemon.getName() + "; "
                + "  exit 1; "
                + "elif type \"update-rc.d\" > /dev/null; then "
                + "  update-rc.d " + daemon.getName() + " defaults; "
                + "  exit 2; "
                + "else "
                + "  exit 3; "
                + "fi";

            Integer exitValue
                = sshExec(true, true, cmd)
                    .exitValues(1, 2, 3)
                    .run();

            switch (exitValue) {
                case 1:
                    log.info("Daemon {} will start at boot (via chkconfig)", daemon.getName());
                    break;
                case 2:
                    log.info("Daemon {} will start at boot (via update-rc.d)", daemon.getName());
                    break;
                case 3:
                    log.error("Daemon {} will be unable to start at boot (neither chkconfig or update-rc.d found)", daemon.getName());
                    break;
            }
        }
        */
    }
}