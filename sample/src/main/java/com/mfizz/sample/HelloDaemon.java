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

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class HelloDaemon extends NanoHTTPD {
    
    private List<String> lines;
    
    public HelloDaemon(int port) {
        super(port);
    }
 
    static public void main(String[] args) throws Exception {
        // call same as hello world
        HelloConsole.main(args);
        
        // was a port specified?
        int port = 8080;
        if (args.length > 0) {
            String portString = args[0];
            port = Integer.parseInt(portString);
        }
        
        // start server
        HelloDaemon server = new HelloDaemon(port);
        server.lines = HelloDaemon.createLines(args);
        System.out.println("Starting http server on port " + port);
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down.");
            }
        });
        
        // keep running
        while (true) {
            System.out.println("Will keep running (will print again in 10 secs)");
            Thread.sleep(10000);
        }
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Hello Daemon!</title></head>");
        sb.append("<body>");
        sb.append("<h1>Hi, i am an example daemon.</h1>");
        sb.append("Date: ").append(new Date()).append("<br/>");
        for (String line : lines) {
            sb.append(line).append("<br/>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return new Response(sb.toString());
    }
    
    static public List<String> createLines(String[] args) {
        List<String> lines = new ArrayList<String>();
        lines.add("working.dir: " + System.getProperty("user.dir"));
        lines.add("home.dir: " + System.getProperty("user.home"));
        lines.add("java.class.path: " + System.getProperty("java.class.path"));
        lines.add("java.home: " + System.getProperty("java.home"));
        lines.add("java.version: " + System.getProperty("java.version"));
        lines.add("java.vendor: " + System.getProperty("java.vendor"));
        lines.add("os.arch: " + System.getProperty("os.arch"));
        lines.add("os.name: " + System.getProperty("os.name"));
        lines.add("os.version: " + System.getProperty("os.version"));
        lines.add("arguments:");
        for (String s : args) {
            lines.add(" - argument: " + s);
        }
        return lines;
    }
    
}
