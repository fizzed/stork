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
public class HelloServer {
 
    static public void main(String[] args) throws Exception {
        System.out.println("working.dir: " + System.getProperty("user.dir"));
        System.out.println("launcher.app.dir: " + System.getProperty("launcher.app.dir"));
        System.out.println("launcher.working.dir: " + System.getProperty("launcher.working.dir"));
        System.out.println("arguments:");
        for (String s : args) {
            System.out.println(" argument: " + s);
        }
        while (true) {
            System.out.println("hello! (sleeping for 10 secs)");
            Thread.sleep(10000L);
        }
    }
    
}
