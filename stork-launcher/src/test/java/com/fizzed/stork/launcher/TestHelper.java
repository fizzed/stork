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

import com.fizzed.crux.vagrant.VagrantClient;
import com.fizzed.crux.vagrant.VagrantClients;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TestHelper {
    
    static public final VagrantClient VAGRANT_CLIENT = VagrantClients.cachingOrEmptyClient();

    static public List<String> hosts() {
        String host = System.getProperty("host");
        if (host != null && !host.equals("")) {
            return Arrays.asList(host);
        } else {
            return Arrays.asList("local", "ubuntu14", "ubuntu16", "windows10", "debian8", "centos7", "freebsd10", "openbsd60");
        }
    }
    
    static public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    static public Path which(Path path, String name) {
        if (isWindows()) {
            return path.resolve(name + ".bat");
        } else {
            return path.resolve(name);
        }
    }
    
}
