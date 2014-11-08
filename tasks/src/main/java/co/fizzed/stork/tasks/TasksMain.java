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
package co.fizzed.stork.tasks;

import java.io.File;
import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import tasks.Context;
import tasks.Settings;

/**
 *
 * @author joelauer
 */
public class TasksMain {
    
    static public void main(String[] args) throws Exception {
        File tasksFile = new File("tasks.js");
        if (!tasksFile.exists()) {
            System.err.println("Unable to find tasks.js in current dir");
            System.exit(1);
        }
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(new FileReader(tasksFile));
        Invocable invocable = (Invocable) engine;

        // does script define a default task?
        String taskToRun = null;
        if (args.length > 0) {
            taskToRun = args[0];
        }
        
        if (taskToRun == null) {
            System.err.println("No task specified (either as default in tasks.js or via command line)");
            System.exit(1);
        }
        
        // export some objects as global to script
        Context context = new Context();
        Settings.populateDefaults(context, context.getSettings());

        // expose Functions object as a global variable to the engine
        engine.put("S", context.getSettings());
        engine.put("OS", context.getOperatingSystems());
        engine.put("F", context.getFunctions());
        engine.put("U", context.getUtils());
        
        //Arrays.copyOfRange(args, 1, args.length);
        
        try {
            Object result = invocable.invokeFunction(taskToRun);
        } catch (RuntimeException e) {
            e.getCause().printStackTrace(System.err);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
