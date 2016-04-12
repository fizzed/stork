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

import java.util.Objects;

public class Daemon {

    private final InitType initType;
    private final String name;
    private final String user;
    private final String group;

    public Daemon(InitType initType, String name, String user, String group) {
        this.name = name;
        this.initType = initType;
        this.user = user;
        this.group = group;
    }

    public InitType getInitType() {
        return initType;
    }
    
    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.initType);
        hash = 37 * hash + Objects.hashCode(this.name);
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
        final Daemon other = (Daemon) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.initType != other.initType) {
            return false;
        }
        return true;
    }
    
}
