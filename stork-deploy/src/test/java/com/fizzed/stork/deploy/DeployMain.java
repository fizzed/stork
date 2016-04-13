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
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployMain {
    static private final Logger log = LoggerFactory.getLogger(DeployMain.class);

    static public void main(String[] args) throws Exception {
        //Path assemblyFile = Paths.get("src/test/resources/fixtures/hello-conf-1.2.4-SNAPSHOT.zip");
        //Path assemblyFile = Paths.get("src/test/resources/fixtures/hello-world-1.2.4-SNAPSHOT.zip");
        Path assemblyFile = Paths.get("src/test/resources/fixtures/hello-console-1.2.4.tar.gz");
        String uri = "vagrant+ssh://ubuntu1404";
        //String uri = "vagrant+ssh://freebsd102";
        //String uri = "vagrant+ssh://centos7";
        //String uri = "vagrant+ssh://openbsd58";
        //String uri = "vagrant+ssh://debian8";
        
        DeployOptions options = new DeployOptions()
            //.prefixDir("/opt")
            //.organization("fizzed")
            //.user("daemon")
            //.group("daemon");
            .user(null);
       
        try (Assembly assembly = Assemblys.process(assemblyFile)) {
            new Deployer().deploy(assembly, options, uri);
        }
    }
    
}
