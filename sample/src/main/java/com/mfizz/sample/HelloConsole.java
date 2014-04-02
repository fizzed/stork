/*
 * Copyright 2013 mfizz.
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
package com.mfizz.sample;

/**
 *
 * @author joelauer
 */
public class HelloConsole {
 
    static public void main(String[] args) throws Exception {
        System.out.println("It worked!");
        System.out.println("working.dir: " + System.getProperty("user.dir"));
        System.out.println("launcher.app.dir: " + System.getProperty("launcher.app.dir"));
        System.out.println("launcher.working.dir: " + System.getProperty("launcher.working.dir"));
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        System.out.println("java.home: " + System.getProperty("java.home"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        
        System.out.println("Arguments:");
        for (String s : args) {
            System.out.println(" argument: " + s);
        }
    }
    
}
