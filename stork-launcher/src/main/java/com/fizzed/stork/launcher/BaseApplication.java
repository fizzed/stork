/*
 * Copyright 2014 Fizzed, Inc.
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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public abstract class BaseApplication {

    public abstract void printUsage();
    
    public void printError(String errorMessage) {
        System.err.println("Error: " + errorMessage);
    }

    public void printUsageAndExitWithError() {
        printUsage();
        System.exit(1);
    }

    public void printErrorThenUsageAndExit(String errorMessage) {
        printError(errorMessage);
        System.out.println("Execute with -h argument for usage info");
        System.exit(1);
    }

    public String popNextArg(String arg, Deque<String> args) {
        if (args.isEmpty()) {
            printErrorThenUsageAndExit("argument [" + arg + "] requires a value as the next argument");
        }
        return args.remove();
    }
    
    final public void run(String[] args) {
        run(new ArrayDeque<>(Arrays.asList(args)));
    }
    
    public abstract void run(Deque<String> args);
}
