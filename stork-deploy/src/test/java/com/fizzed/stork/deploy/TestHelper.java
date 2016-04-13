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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class TestHelper {
    
    // all hosts we support for deploys w/ only console apps
    static private final List<String> VAGRANT_ALL_HOSTS =
        Arrays.asList("ubuntu1404", "debian8", "centos7", "freebsd102", "openbsd58");
    
    // subset of hosts we support for deploys w/ daemon apps
    static private final List<String> VAGRANT_DAEMON_HOSTS =
        Arrays.asList("ubuntu1404", "debian8", "centos7");
    
    static public List<String> filterVagrantHosts(List<String> hosts) {
        String host = System.getProperty("host");
        if (host != null && !host.equals("")) {
            return Arrays.asList(host);
        } else {
            return hosts;
        }
    }
    
    static public List<String> getAllVagrantHosts() {
        return filterVagrantHosts(VAGRANT_ALL_HOSTS);
    }
    
    static public List<String> getDaemonVagrantHosts() {
        return filterVagrantHosts(VAGRANT_DAEMON_HOSTS);
    }
    
    static public Path getResource(String resourceName) throws Exception {
        URI uri = TestHelper.class.getResource(resourceName).toURI();
        return Paths.get(uri);
    }
    
}
