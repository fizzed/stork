/*
 * Copyright 2014 mfizz.
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

package co.fizzed.stork.bootstrap;

import java.util.Properties;

/**
 *
 * @author joelauer
 */
public class PlayBootstrap extends Bootstrap {
    
    @Override
    public void overrideSystemProperties(Properties props) {
        super.overrideSystemProperties(props);
        
        // set pidfile property?
        if (!props.containsKey("pidfile.path")) {
            // set pid to /dev/null or NUL depending on which platform running on
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                props.setProperty("pidfile.path", "NUL");
            } else {
                props.setProperty("pidfile.path", "/dev/null");
            }
        }
    }
    
}
