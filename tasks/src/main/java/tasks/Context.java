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

/**
 *
 * @author joelauer
 */
public class Context {
    
    private final Settings settings;
    private final Functions functions;
    private final OperatingSystems operatingSystems;
    private final Utils utils;
    
    public Context() {
        this.settings = new Settings(this);
        this.functions = new Functions(this);
        this.operatingSystems = new OperatingSystems(this);
        this.utils = new Utils(this);
    }

    public Settings getSettings() {
        return settings;
    }

    public Functions getFunctions() {
        return functions;
    }

    public OperatingSystems getOperatingSystems() {
        return operatingSystems;
    }

    public Utils getUtils() {
        return utils;
    }

}
