/*
 * Copyright 2014 Fizzed Inc.
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
package tasks;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author joelauer
 */
public class Settings {
    
    static public final Set<String> EXE_EXT_WINDOWS = new LinkedHashSet<String>(Arrays.asList("",".exe",".bat",".cmd"));
    static public final Set<String> EXE_EXT_UNIX = new LinkedHashSet<String>(Arrays.asList("", ".sh"));
    
    private final Context context;
    private final Set<String> executableExtensions;
    
    public Settings(Context context) {
        this.context = context;
        this.executableExtensions = new LinkedHashSet<String>();
    }
    
    static public void populateDefaults(Context context, Settings s) {
        if (context.getOperatingSystems().isWindows()) {
            s.executableExtensions.addAll(EXE_EXT_WINDOWS);
        } else {
            s.executableExtensions.addAll(EXE_EXT_UNIX);
        }
    }

    public Set<String> getExecutableExtensions() {
        return executableExtensions;
    }
    
}
