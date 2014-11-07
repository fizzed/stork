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

package co.fizzed.stork.bootstrap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

/**
 *
 * @author joelauer
 */
public class PlayBootstrap extends Bootstrap {
    
    static public void main(String[] args) throws Exception {
        new PlayBootstrap().run(args);
    }
    
    @Override
    public void overrideSystemProperties(Properties props) {
        super.overrideSystemProperties(props);
        
        // set pidfile property?
        if (!props.containsKey("pidfile.path")) {
            // set pid to /dev/null or NUL depending on which platform running on
            if (props.getProperty("os.name").toLowerCase().contains("win")) {
                System.out.println("Setting system property [pidfile.path=NUL]");
                props.setProperty("pidfile.path", "NUL");
            } else {
                System.out.println("Setting system property [pidfile.path=/dev/null]");
                props.setProperty("pidfile.path", "/dev/null");
            }
        } else {
            System.out.println("Using system property [pidfile.path=" + props.getProperty("pidfile.path") + "]");
        }
    }
    
    static public File createBaseLauncherConfFile(File targetDir, String playAppName) throws Exception {
        File f = new File(targetDir, "play-launcher.yml");
        PrintWriter pw = new PrintWriter(new FileWriter(f));
        pw.println("name: \"" + playAppName + "\"");
        pw.println("domain: \"com.playframework\"");
        pw.println("display_name: \"" + playAppName + "\"");
        pw.println("short_description: \"" + playAppName + "\"");
        pw.println("type: DAEMON");
        //pw.println("main_class: \"play.core.server.NettyServer\"");
        pw.println("main_class: \"co.fizzed.stork.bootstrap.PlayBootstrap\"");
        pw.println("log_dir: \"log\"");
        pw.println("platforms: [ LINUX ]");
        pw.println("working_dir_mode: APP_HOME");
        pw.println("app_args: \"\"");
        pw.println("java_args: \"-Xrs -Djava.net.preferIPv4Stack=true -Dlauncher.main=play.core.server.NettyServer -Dlauncher.config=conf/play.conf -Dlogger.file=conf/logger.xml\"");
        pw.println("min_java_version: \"1.7\"");
        pw.println("symlink_java: true");
        pw.close();
        return f;
    }

    static public void createLoggerConfFile(File loggerConfFile, String playAppName) throws Exception {
        PrintWriter pw = new PrintWriter(new FileWriter(loggerConfFile));
        pw.println( "<configuration>\n" +
                    "  <appender name=\"FILE\" class=\"ch.qos.logback.core.FileAppender\">\n" +
                    "     <file>log/" + playAppName + ".log</file>\n" +
                    "     <encoder>\n" +
                    "       <pattern>%date [%level] %logger - %message%n%xException</pattern>\n" +
                    "     </encoder>\n" +
                    "   </appender>\n" +
                    "\n" +
                    "  <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
                    "    <encoder>\n" +
                    "      <pattern>%date [%level] %logger - %message%n%xException</pattern>\n" +
                    "    </encoder>\n" +
                    "  </appender>\n" +
                    "  \n" +
                    "  <logger name=\"play\" level=\"INFO\" />\n" +
                    "  <logger name=\"application\" level=\"INFO\" />\n" +
                    "\n" +
                    "  <root level=\"DEBUG\">\n" +
                    "    <appender-ref ref=\"STDOUT\" />\n" +
                    "    <appender-ref ref=\"FILE\" />\n" +
                    "  </root>\n" +
                    "</configuration>");
        pw.close();
    }
    
}
