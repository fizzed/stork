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

import static com.fizzed.stork.deploy.BasicFile.pathToUnixString;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deployments {
    static private final Logger log = LoggerFactory.getLogger(Deployments.class);
 
    static public String baseDir(String name, DeployOptions options) {
        Objects.requireNonNull(options, "options cannot be null");
        
        // app base always starts at prefix (e.g. /opt)
        String baseDir = Optional.ofNullable(options.getPrefixDir()).orElse("/opt");
        
        // append the org name? (e.g. /opt/org)
        if (options.getOrganization() != null) {
            baseDir += "/" + options.getOrganization();
        }
        
        baseDir += "/" + name;
        
        return baseDir;
    }
    
    static public Deployment install(Assembly assembly, Target target, DeployOptions options) {
        String baseDir = baseDir(assembly.getName(), options);
        String currentDir = baseDir + "/current";
        String versionDir = baseDir + "/v" + assembly.getVersion();
        
        if (assembly.isSnapshot()) {
            Instant instant = Instant.ofEpochMilli(assembly.getCreatedAt());
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            String date = ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS", Locale.US));
            versionDir += "-" + date;
        }
        
        return new Deployment(baseDir, currentDir, versionDir,
            options.getUser(), options.getGroup());
    }
    
    static public ExistingDeployment existing(Deployment deployment, Target target) {
        return existing(deployment.getBaseDir(), target);
    }
    
    
    
    static public ExistingDeployment existing(String baseDir, Target target) {
        String foundBaseDir = null;
        String currentDir = null;
        String versionDir = null;
        Set<String> versionDirs = new TreeSet<>();
        Long deployedAt = null;
        
        // inventory what's currently on target's baseDir
        List<BasicFile> files = target.listFiles(baseDir);
        
        if (files != null) {
            foundBaseDir = baseDir;
            
            for (BasicFile file : files) {
                if (file.getPath().getFileName().toString().equals("current")) {
                    currentDir = pathToUnixString(file.getPath());
                    
                    // where does it point to?
                    Path versionDirPath = target.realpath(file.getPath());
                    if (versionDirPath != null) {
                        versionDir = pathToUnixString(versionDirPath);
                    }
                } else {
                    // otherwise its a versioned dir
                    versionDirs.add(pathToUnixString(file.getPath()));
                }
            }
            
            // find version path to get its created at time
            for (BasicFile file : files) {
                if (file.getPath().toString().equals(versionDir)) {
                    deployedAt = file.getCreatedAt();
                }
            }
            
            /**
            // determine previous user & group used for install by looking
            // for the version dir?
            if (versionDir != null) {
                for (BasicFile file : files) {
                    if (file.getPath().toString().equals(versionDir)) {
                        int userId = file.getUserId();
                        int groupId = file.getGroupId();
                        
                        log.debug("{} {}:{}", file.getPath(), userId, groupId);
                        
                        break;
                    }
                }
            }
            */
        }
        
        return new ExistingDeployment(foundBaseDir, currentDir, versionDir, null, null, deployedAt, versionDirs);
    }
    
}
