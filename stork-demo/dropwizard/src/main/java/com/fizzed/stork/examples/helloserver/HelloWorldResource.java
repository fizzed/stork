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
package com.fizzed.stork.examples.helloserver;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.jersey.caching.CacheControl;
import java.util.Date;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class HelloWorldResource {

    static Date startedAt = new Date();
    
    public HelloWorldResource() {
        // do nothing
    }

    @GET
    @Timed
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Response index(@QueryParam("name") Optional<String> name) {
        return Response.ok(createBody()).build();
 
    }
    
    static String[] sysprops = { "user.dir","user.name","user.home","launcher.name","launcher.type","launcher.action","launcher.app.dir","java.class.path","java.home","java.version","java.vendor","os.arch","os.name","os.version" };
    
    static public String createBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Hello Server using DropWizard!</title></head>");
        sb.append("<body>");
        sb.append("<h1>Stork by <a href=\"http://fizzed.com\">Fizzed</a></h1>");
        sb.append("<h2>Example using DropWizard</h2>");
        // Version.java auto created by Fizzed parent POM file (I use this in all my projects)
        sb.append("<b>Version:</b> ").append(com.fizzed.stork.examples.helloserver.Version.getLongVersion()).append("<br/>");
        sb.append("<b>Now:</b> ").append(new Date()).append("<br/>");
        sb.append("<b>Started:</b> ").append(startedAt).append("<br/>");
        sb.append("<b>Some system properties:</b><br/>");
        for (String sysprop : sysprops) {
            sb.append(" - ").append(sysprop).append(": ").append(System.getProperty(sysprop)).append("<br/>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
    
}