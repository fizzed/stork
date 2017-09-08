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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class SystemdHelper {
    static private int indexOfAny(String value, String[] finds) {
        for (String find : finds) {
            int pos = value.indexOf(find);
            if (pos >= 0) { return pos; }
        }
        return -1;
    }
    
    static public void modifyForInstall(Logger log, Assembly assembly, Deployment install) {
        Set<Daemon> daemons = assembly.getDaemons(InitType.SYSTEMD);
        
        if (daemons == null) {
            return;
        }
        
        for (Daemon daemon : daemons) {
            // systemd service files (unfortunately) cannot be scripted with paths
            // for ExecStart, User, Group, etc.  so we need to modify those when
            // we finally install the .service file
            Path systemdServiceFile = assembly.getUnpackedDir().resolve("share/systemd/" + daemon.getName() + ".service");
            try {
                List<String> modifiedSystemdLines
                    = Files.lines(systemdServiceFile)
                        .map((line) -> {
                            int equalsPos = line.indexOf("=");
                            if (equalsPos > 0) {
                                String key = line.substring(0, equalsPos);
                                String value = line.substring(equalsPos+1).trim();
                                
                                // does the value reference a /bin or /run?
                                int dirPos = indexOfAny(line, new String[] { "/bin", "/run" });
                                
                                if (dirPos > 0) {
                                    return line.substring(0, equalsPos+1)
                                        + install.getCurrentDir()
                                        + line.substring(dirPos);
                                } else if (key.equalsIgnoreCase("user")) {
                                    return "User=" + install.getUser().orElse("");
                                } else if (key.equalsIgnoreCase("group")) {
                                    return "Group=" + install.getGroup().orElse("");
                                }
                            }
                            return line;
                        })
                        .collect(Collectors.toList());
                
                Files.write(systemdServiceFile, modifiedSystemdLines, StandardOpenOption.TRUNCATE_EXISTING);
                
                log.info("Modified for install: {}", systemdServiceFile);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    
}
