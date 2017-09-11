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

import java.io.File;
import java.nio.file.Path;

public class BasicFile {
    
    private final Path path;
    private final long createdAt;
    private final int userId;
    private final int groupId;

    public BasicFile(Path path, long createdAt, int userId, int groupId) {
        this.path = path;
        this.createdAt = createdAt;
        this.userId = userId;
        this.groupId = groupId;
    }
    
    public Path getPath() {
        return path;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return path.toString();
    }
    
    static public String pathToUnixString(Path path) {
        if (path == null) {
            return null;
        }
        String s = path.toString();
        if (File.separatorChar == '\\') {
            s = s.replace('\\', '/');
        }
        return s;
    }
    
}
