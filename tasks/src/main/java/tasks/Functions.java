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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.zeroturnaround.exec.ProcessExecutor;

/**
 *
 * @author joelauer
 */
public class Functions {
    
    public ProcessExecutor executor(String ... command) throws Exception {
        return new ProcessExecutor()
            .command(command)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValueNormal();
    }
    
    public int execute(String ... command) throws Exception {
        return executor(command)
            .execute()
            .getExitValue();
    }
    
    public List<File> listFiles() {
        return Arrays.asList(new File(".").listFiles());
    }
    
}
