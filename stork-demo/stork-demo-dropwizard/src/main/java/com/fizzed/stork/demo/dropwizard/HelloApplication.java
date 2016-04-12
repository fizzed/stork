/*
 * Copyright 2014 mfizz.
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
package com.fizzed.stork.demo.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class HelloApplication extends Application<HelloConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-dropwizard";
    }

    @Override
    public void initialize(Bootstrap<HelloConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(HelloConfiguration configuration, Environment environment) throws ClassNotFoundException {
        environment.jersey().register(new HelloResource());
    }
}