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
package com.fizzed.stork.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchData {

    private String confirm;
    private Map<String,String> environment;
    private Map<String,String> systemProperties;
    private List<String> arguments;

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public Map<String,String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String,String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    static public final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true);
    
    static public LaunchData parse(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, LaunchData.class);
    }
    
    static public LaunchData create(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        // convert system properties to a map<string,string>
        Map<String,String> systemProperties = new HashMap<>();
        for (Map.Entry<Object,Object> entry : System.getProperties().entrySet()) {
            systemProperties.put(entry.getKey().toString(), entry.getValue().toString());
        }
        
        LaunchData output = new LaunchData();
        output.setConfirm("Hello World!");
        output.setEnvironment(System.getenv());
        output.setSystemProperties(systemProperties);
        output.setArguments(Arrays.asList(args));
        
        return output;
    }
    
    static public String prettyJson(LaunchData output) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(output);
    }
    
    static public void prettyWrite(LaunchData output, OutputStream os) throws IOException {
        OBJECT_MAPPER.writeValue(os, output);
    }
    
    static public void prettyWrite(LaunchData output, Arguments arguments) throws IOException {
        if (arguments.getDataFile() == null) {
            LaunchData.prettyWrite(output, System.out);
        } else {
            try (OutputStream os = Files.newOutputStream(arguments.getDataFile())) {
                LaunchData.prettyWrite(output, os);
                System.out.println("Wrote launch data to " + arguments.getDataFile());
            }
        }
    }
    
}
