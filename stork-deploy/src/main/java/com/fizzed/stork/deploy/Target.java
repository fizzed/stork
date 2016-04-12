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

import com.fizzed.blaze.util.ImmutableUri;
import java.io.Closeable;
import java.nio.file.Path;
import java.util.List;

abstract public class Target implements Closeable {
    
    private final ImmutableUri uri;
    private final String uname;
    private final InitType initType;
    private final String tempDir;

    public Target(ImmutableUri uri, String uname, InitType initType, String tempDir) {
        this.uri = uri;
        this.uname = uname;
        this.initType = initType;
        this.tempDir = tempDir;
    }

    public ImmutableUri getUri() {
        return uri;
    }

    public String getUname() {
        return uname;
    }

    public InitType getInitType() {
        return initType;
    }

    public String getTempDir() {
        return this.tempDir;
    }

    @Override
    public String toString() {
        return this.uri.toString();
    }
    
    abstract public boolean hasUser(String user);

    abstract public boolean hasGroup(String group);

    abstract public List<BasicFile> listFiles(Object path);
    
    abstract public Path readlink(Object path);
    
    abstract public Path realpath(Object path);
    
    abstract public void createDirectories(boolean sudo, Object path);

    abstract public void remove(boolean sudo, Object... paths);

    abstract public void put(Path source, String target);
    
    abstract public void unpack(String path, String targetDir);

    abstract public void copyFiles(boolean sudo, String source, String target);

    abstract public void moveFiles(boolean sudo, String source, String target);

    abstract public void symlink(boolean sudo, String target, String link);

    abstract public void chown(boolean sudo, boolean recursive, String owner, String target);

    abstract public void chmod(boolean sudo, boolean recursive, String permissions, String target);
    
    abstract public void startDaemon(Daemon daemon) throws DeployerException;

    abstract public void stopDaemon(Daemon daemon) throws DeployerException;

    abstract public void installDaemon(Deployment install, Daemon daemon, boolean onBoot) throws DeployerException;

}
