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
package com.fizzed.stork.demo;

public class Hello {
 
    static public void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        System.out.println("working.dir: " + System.getProperty("user.dir"));
        System.out.println("home.dir: " + System.getProperty("user.home"));
        System.out.println("launcher.name: " + System.getProperty("launcher.name"));
        System.out.println("launcher.type: " + System.getProperty("launcher.type"));
        System.out.println("launcher.action: " + System.getProperty("launcher.action"));
        System.out.println("launcher.app.dir: " + System.getProperty("launcher.app.dir"));
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        System.out.println("java.home: " + System.getProperty("java.home"));
        System.out.println("java.version: " + System.getProperty("java.version"));
        System.out.println("java.vendor: " + System.getProperty("java.vendor"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.version: " + System.getProperty("os.version"));
        System.out.println("arguments:");
        for (String s : args) {
            System.out.println(" - argument: " + s);
        }
    }
    
}
