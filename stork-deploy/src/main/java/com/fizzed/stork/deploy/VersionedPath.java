/*
 * Copyright 2017 Fizzed, Inc.
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

import java.util.Objects;

public class VersionedPath implements Comparable<VersionedPath> {
 
    private final long createdAt;
    private final String path;

    public VersionedPath(long createdAt, String path) {
        this.createdAt = createdAt;
        this.path = path;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return this.path;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.createdAt ^ (this.createdAt >>> 32));
        hash = 89 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VersionedPath other = (VersionedPath) obj;
        if (this.createdAt != other.createdAt) {
            return false;
        }
        return Objects.equals(this.path, other.path);
    }

    @Override
    public int compareTo(VersionedPath o) {
        // descending order
        int c = (int)(o.createdAt - this.createdAt);
        if (c == 0) {
            return o.path.compareTo(this.path);
        }
        return c;
    }
}