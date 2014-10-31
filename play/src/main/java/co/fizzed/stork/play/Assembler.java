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
package co.fizzed.stork.play;

import co.fizzed.stork.launcher.BaseApplication;
import co.fizzed.stork.launcher.Configuration;
import co.fizzed.stork.launcher.Generator;
import co.fizzed.stork.launcher.Merger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public class Assembler extends BaseApplication {

    private File playProjectDir;
    private String playCommand;
    private File playConfDir;
    private File baseLauncherConfFile;
    private File launcherConfFile;
    private File mergedLauncherConfFile;

    @Override
    public void printUsage() {
        System.err.println("Usage: stork-play-assembly [-p <playProjectDir>]");
        System.err.println("-v                      Print version and exit");
        System.err.println("-p <playProjectDir>     Dir of play project");
    }

    static public void main(String[] args) {
        new Assembler().run(args);
    }

    public Assembler() {
        playProjectDir = new File(".");
    }

    @Override
    public void run(String[] args) {
        List<String> argList = new ArrayList<String>(Arrays.asList(args));

        // parse command-line arguments
        while (argList.size() > 0) {
            String argSwitch = argList.remove(0);

            if (argSwitch.equals("-v") || argSwitch.equals("--version")) {
                System.err.println("stork-play-assembly version: " + co.fizzed.stork.play.Version.getLongVersion());
                System.exit(0);
            } else if (argSwitch.equals("-p") || argSwitch.equals("--play-project-dir")) {
                String fileString = popNextArg(argSwitch, argList);
                playProjectDir = new File(fileString);
            } else if (argSwitch.equals("-h") || argSwitch.equals("--help") ) {
                printUsage();
                System.exit(0);
            } else {
                printErrorThenUsageAndExit("invalid argument switch [" + argSwitch + "] found");
            }
        }

        try {
            assemble();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }



    public void assemble() throws Exception {
        // verify project dir is a directory
        if (!playProjectDir.exists()) {
            printError("Play project dir [" + playProjectDir + "] does not exist");
            System.exit(1);
        }

        if (!playProjectDir.isDirectory()) {
            printError("Play project dir [" + playProjectDir + "] exists but is not a directory");
            System.exit(1);
        }

        // find play command
        playCommand = findPlayCommand(playProjectDir);
        if (playCommand == null) {
            printError("Unable to find either [activator] or [play] command.  Installed and on PATH?");
            System.exit(1);
        }

        // does conf/application.conf exist in play project dir?
        playConfDir = new File(playProjectDir, "conf");
        if (!playConfDir.exists() || !playConfDir.isDirectory()) {
            printError("Conf directory in play project dir [" + playProjectDir + "] either does not exist or is not a directory");
            System.exit(1);
        }

        // check if conf/application.conf exists...
        File playConfFile = new File(playConfDir, "application.conf");
        if (!playConfFile.exists()) {
            printError("Dir [" + playProjectDir + "] does not appear to be a Play project [" + playConfFile.getAbsolutePath() + " does not exist]");
            System.exit(1);
        }

        if (launcherConfFile == null) {
            launcherConfFile = new File(playConfDir, "launcher.yml");
        } else {
            if (!launcherConfFile.exists()) {
                printError("Launcher conf file [" + launcherConfFile.getAbsolutePath() + "] does not exist");
                System.exit(1);
            }
        }

        // determine name of play app
        //echo "Detecting play app name..."
        //app_name=`$play_cmd name | tail -n 1 | sed 's/.* //g' | sed -r "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]//g"`
        String playAppName = detectPlayAppName(playProjectDir, playCommand);

        // determine version of play app
        //echo "Detecting play app version..."
        //app_version=`$play_cmd version | tail -n 1 | sed 's/.* //g' | sed -r "s/\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]//g"`
        String playAppVersion = detectPlayAppVersion(playProjectDir, playCommand);

        // clean and stage play app (so lib directory can be created)
        cleanAndStagePlayApp(playProjectDir, playCommand);

        // usual play output dirs...
        File targetDir = new File(playProjectDir, "target");
        File universalDir = new File(targetDir, "universal");
        File stageDir = new File(universalDir, "stage");

        // handle launcher config file & merging...
        System.out.println("Creating base play launcher config file...");
        baseLauncherConfFile = createBaseLauncherConfFile(targetDir, playAppName);

        if (this.launcherConfFile != null && this.launcherConfFile.exists()) {
            System.out.println("Merging base play launcher config file with the app-specific launcher config file...");
            mergedLauncherConfFile = new File(targetDir, "play-launcher-merged.yml");
            Merger launcherMerger = new Merger();
            launcherMerger.merge(Arrays.asList(baseLauncherConfFile, launcherConfFile), mergedLauncherConfFile);
        } else {
            mergedLauncherConfFile = baseLauncherConfFile;
        }


        //
        // generate launcher configs (that will be used later on)
        //
        System.out.println("Creating play launcher...");
        Generator launcherGenerator = new Generator();
        List<Configuration> launcherConfigs = launcherGenerator.createConfigs(Arrays.asList(mergedLauncherConfFile));
        Configuration launcherConfig = launcherConfigs.get(0);

        // playAppName may have been overridden...
        if (!playAppName.equals(launcherConfig.getName())) {
            System.out.println("Launcher overrides play app name from [" + playAppName + "] to [" + launcherConfig.getName() + "]");
            playAppName = launcherConfig.getName();
        }

        // start assembling final package
        String assemblyName = playAppName + "-" + playAppVersion;
        File assemblyDir = new File(targetDir, assemblyName);

        System.out.println("Creating assembly dir: " + assemblyDir.getAbsolutePath());
        assemblyDir.mkdirs();

        System.out.println("Copying lib dir to assembly dir...");
        //cp -R "$stage_dir"/lib $target_dir/$assembly_dir/
        File stageLibDir = new File(stageDir, "lib");
        File assemblyLibDir = new File(assemblyDir, "lib");
        FileUtils.copyDirectory(stageLibDir, assemblyLibDir);

        System.out.println("Copying conf dir to assembly dir...");
        //cp -R "$stage_dir"/conf $target_dir/$assembly_dir/
        File stageConfDir = new File(stageDir, "conf");
        File assemblyConfDir = new File(assemblyDir, "conf");
        FileUtils.copyDirectory(stageConfDir, assemblyConfDir);

        File loggerConfFile = new File(assemblyConfDir, "logger.xml");
        if (loggerConfFile.exists()) {
            System.out.println("Using existing logger.xml file copied directly from conf dir...");
        } else {
            System.out.println("Creating default logger.xml file...");
            createLoggerConfFile(loggerConfFile, playAppName);
        }

        launcherGenerator.runConfigs(launcherConfigs, assemblyDir);


        File assemblyTarGz = new File(assemblyDir.getAbsolutePath() + ".tar.gz");
        System.out.println("Creating assembly tarball...");
        TarArchiveOutputStream out = null;
        try {
            out = new TarArchiveOutputStream(
                  new GZIPOutputStream(
                       new BufferedOutputStream(new FileOutputStream(assemblyTarGz))));
            out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            addFileToTarGz(out, assemblyDir.getAbsolutePath(), "");
        } finally {
             if (out != null) {
                 out.close();
             }
        }

        System.out.println("Generated play assembly: " + assemblyTarGz);
        System.out.println("Done!");
    }

    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);

        if (f.isFile()) {
            if (f.canExecute()) {
                // -rwxr-xr-x
                tarEntry.setMode(493);
            } else {
                // keep default mode
            }
        }

        tOut.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            FileInputStream in = new FileInputStream(f);
            IOUtils.copy(in, tOut);
            in.close();
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null){
                for (File child : children) {
                    System.out.println(" adding: " + child.getName());
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

    public File createBaseLauncherConfFile(File targetDir, String playAppName) throws Exception {
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

    public void createLoggerConfFile(File loggerConfFile, String playAppName) throws Exception {
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

    public void cleanAndStagePlayApp(File playProjectDir, String playCommand) throws Exception {
        System.out.println("Cleaning and staging play application... (sometimes takes awhile if play is downloading dependencies)");

        ProcessResult result = new ProcessExecutor()
            .directory(playProjectDir)
            .command(playCommand, "clean", "stage")
            .readOutput(true)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValues(0)
            .execute();

        String output = result.outputUTF8().trim();
        //output = removeAnsiColorCodes(output);
    }

    public String detectPlayAppName(File playProjectDir, String playCommand) throws Exception {
        System.out.println("Detecting play application name... (sometimes takes awhile if play is downloading dependencies)");

        ProcessResult result = new ProcessExecutor()
            .directory(playProjectDir)
            .command(playCommand, "name")
            .readOutput(true)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValues(0)
            .execute();

        String output = result.outputUTF8().trim();
        output = removeAnsiColorCodes(output);

        /*
         Loading project definition from /home/joelauer/workspace/fizzed/java-stork/examples/hello-server-play/project
        [info] Set current project to hello-server-play (in build file:/home/joelauer/workspace/fizzed/java-stork/examples/hello-server-play/)
        [info] hello-server-play
        */
        String[] lines = output.split("\n");
        if (lines == null || lines.length < 3) {
            throw new Exception("Unexpected play name format");
        }

        String lastLine = lines[lines.length - 1];
        String[] split = lastLine.split("\\] ");
        if (split.length != 2) {
            throw new Exception("Unexpected last line format: " + lastLine);
        }

        // trim and then clean potential bash formatting
        String playAppName = removeAnsiColorCodes(split[1].trim());
        System.out.println("Detected play app name: " + playAppName);

        return playAppName;
    }

    public String detectPlayAppVersion(File playProjectDir, String playCommand) throws Exception {
        System.out.println("Detecting play application version... (sometimes takes awhile if play is downloading dependencies)");

        ProcessResult result = new ProcessExecutor()
            .directory(playProjectDir)
            .command(playCommand, "version")
            .readOutput(true)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValues(0)
            .execute();

        String output = result.outputUTF8().trim();
        output = removeAnsiColorCodes(output);

        /*
         Loading project definition from /home/joelauer/workspace/fizzed/java-stork/examples/hello-server-play/project
        [info] Set current project to hello-server-play (in build file:/home/joelauer/workspace/fizzed/java-stork/examples/hello-server-play/)
        [info] hello-server-play
        */
        String[] lines = output.split("\n");
        if (lines == null || lines.length < 3) {
            throw new Exception("Unexpected play name format");
        }

        String lastLine = lines[lines.length - 1];
        String[] split = lastLine.split("\\] ");
        if (split.length != 2) {
            throw new Exception("Unexpected last line format: " + lastLine);
        }

        // trim and then clean potential bash formatting
        String playAppName = removeAnsiColorCodes(split[1].trim());
        System.out.println("Detected play app version: " + playAppName);

        return playAppName;
    }

    public String removeAnsiColorCodes(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    public String findPlayCommand(File playProjectDir) {
        String playCommand = null;
        
        // do we need to append ".bat" on the end if running on windows?
        String batEnd = "";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            batEnd = ".bat";
        }
        
        // try to find "activator" command first (>= play 2.3)
        playCommand = "activator" + batEnd;
        if (!isPlayCommandPresent(playCommand)) {
            // try to find "activator" command inside project directory...
            playCommand = new File(playProjectDir, "activator" + batEnd).getAbsolutePath();
            if (!isPlayCommandPresent(playCommand)) {
                // fallback to "play" command (< play 2.3)
                playCommand = "play" + batEnd;
                if (!isPlayCommandPresent(playCommand)) {
                    return null;
                }
            }
        }

        return playCommand;
    }
    
    public boolean isPlayCommandPresent(String playCommand) {
        // try to find "activator" command first (>= play 2.3)
        try {
            ProcessResult result = new ProcessExecutor()
                .command(playCommand, "--version")
                .readOutput(true)
                .exitValue(0)
                .execute();
            System.out.println("Play [" + playCommand + "] command found: " + result.outputUTF8().trim());
            return true;
        } catch (Exception e) {
            System.out.println("Play [" + playCommand + "] command not found");
            return false;
        }
    }

}
