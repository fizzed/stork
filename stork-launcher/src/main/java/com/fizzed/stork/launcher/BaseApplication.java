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

import java.util.List;

/**
 *
 * @author joelauer
 */
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

    public String popNextArg(String argSwitch, List<String> argList) {
        if (argList.isEmpty()) {
            printErrorThenUsageAndExit("argument switch [" + argSwitch + "] requires a value as next argument");
        }
        return argList.remove(0);
    }
    
    public abstract void run(String[] args);
}
