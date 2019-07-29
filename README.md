Stork by Fizzed
=======================================

[![Build Status](https://travis-ci.org/fizzed/stork.svg?branch=master)](https://travis-ci.org/fizzed/stork)
[![Build status](https://ci.appveyor.com/api/projects/status/1yv9d52o4lshaawp/branch/master?svg=true)](https://ci.appveyor.com/project/jjlauer/stork/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/stork/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/stork)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Sponsored by

Stork is proudly sponsored by <a href="https://www.greenback.com">Greenback</a>.  We love the service and think you would too.

<a href="https://www.greenback.com?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-stork" title="Greenback - Expenses made simple"><img src="https://www.greenback.com/assets/images/logo-greenback.png" height="48" width="166" alt="Greenback"></a>

<a href="https://www.greenback.com?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-stork" title="Greenback - Expenses made simple">More engineering. Less paperwork. Expenses made simple.</a>

## Overview

So you've engineered that amazing Java-based application.  Then what?  Distributing
it or getting it into production is your new problem.  Fat/uber jar? Vagrant?
Docker? Rkt? LXD? Traditional bare metal deploy? There are so many options!

Stork is a collection of lightweight utilities for optimizing your "after-build" workflow by
filling in the gap between your Java build system and execution.  Using well-tested
methods across operating systems, containers, etc. Stork will let you safely
and securely run your app in any environment -- be it Docker, Rkt, LXD, or
traditional systems.  There are 3 main Stork components that you can pick and
choose from to help with your app:

 - [stork-launcher](#stork-launcher) will generate well-tested, rock solid, secure
   launcher scripts from a yaml configuration file for either console or daemon/service
   JVM apps. The generated launchers will run your app the same way regardless of whether
   running within a container or numerous different operating systems.

 - [stork-assembly](#stork-assembly) will assemble your JVM app into a standard,
   well-defined [canonical layout](docs/CANONICAL_LAYOUT.md) as a tarball ready
   for universal distribution or deployment.  Regardless of whether your user
   is on Linux, Windows, OSX, *BSD, etc., our tarball will include everything
   for your user to be happy.

 - [stork-deploy](#stork-deploy) will rapidly and securely deploy your assembly
   via SSH into a versioned directory structure to various operating systems.
   It will handle restarting daemons, use strict user/group permissions, and verify
   the deploy worked.  Power users can combine with [Blaze](https://github.com/fizzed/blaze)
   for even more advanced deploys.

[Using Stork to deploy a production Ninja Framework app](http://fizzed.com/blog/2015/01/using-stork-deploy-production-ninja-framework-app)

## Example

[stork-demo-hellod](stork-demo/stork-demo-hellod) is an example Maven project
for a simple Java web application.  It demos the `stork-launcher`
and `stork-assembly` utilities and produces a tarball assembly that can be
deployed using `stork-deploy`.  To generate the launchers and assembly, run
this from the `stork` main directory:

    mvn package -DskipTests=true

This will generate all launchers, prep the assembly in `target/stork`, and
tarball it up to `target/stork-demo-hellod-X.X.X.tar.gz` (X.X.X is the version
of your project).  You can quickly try it out:

    cd stork-demo/stork-demo-hellod
    target/stork/bin/stork-demo-hellod --run

Or you can deploy it via SSH using `stork-deploy`:

    stork-deploy --assembly target/stork-demo-hellod-X.X.X.tar.gz ssh://host.example.com

Or you can build a Docker image:

    docker build -t stork-demo-hellod .
    docker run -it stork-demo-hellod

## Sponsoring development? Commercial support? Devops consulting?

Maintaining Stork takes a significant amount of time and resources.  If you're
interested in sponsoring Stork, funding new features, or are looking to take
your devops to the next level, please [reach out to us @ Fizzed, Inc.](http://fizzed.com/contact)

<a href="http://fizzed.com/contact" title="Fizzed"><img src="http://fizzed.com/assets/mfizz/img/logo-new.300x210.png" width="150" height="105" alt="Fizzed"></a>

## Usage

### Command-line

https://github.com/fizzed/stork/releases/download/v3.0.0/stork-3.0.0.tar.gz

### Maven plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>stork-maven-plugin</artifactId>
            <version>3.0.0</version>
            <!-- configuration / execution (see below) -->
        </plugin>
    </plugins>
</build>
```

### Gradle Plugin (to be released soon)
```groovy
plugins {
  id "com.fizzed.stork" version "x.x.x"
}
// configuration / execution (see below)
```

## Why not just create my own script?

That's what we used to do with all of our Java apps too.  Eventually, you'll have
a problem -- we guarantee it.  For example, you simply ran `java -jar app.jar &`
in a shell and everything is working.  You close your terminal/SSH session and
your app is no longer running.  Oops, you forgot to detach your app from the
terminal.  Use systemd?  Did you remember to add the `-Xrs` flag when you launched
your java process?  Customer needs to run your app on Windows?  In that case
you have no option but to use some sort of service framework.  Or even something
simple like `java` isn't found by your init system, but it works in your shell.
Stork launchers solve these common problems.

## Why not just a fat/uber jar?

An uber/fat jar is a jar that has all dependencies merged into it.  Usually
an application consists of more than your jar (such as config files), so you'll
still need to package that up.  Then how do you run it as a daemon/service? 
Plus, its becoming more important to cache/retain most of the dependencies that
didn't change for faster deploys using Docker or rsync.

## Stork launcher

Utility for generating native launchers for Java-based applications
across Windows, Linux, Mac, and many other UNIX-like systems.

You simply create a YAML-based config file (that you can check-in to
source control) and then you compile/generate it into one or more launchers.
These launchers can then be distributed with your final tarball/assembly/package
so that your app looks like a native compiled executable.

### Features

 * Generate *secure* launcher scripts for either console or daemon/service JVM apps.
 * Heavily tested across all major operating systems for every release
    * Windows XP+
    * Linux (Ubuntu, Debian, Redhat flavors)
    * Mac OSX
    * FreeBSD
    * OpenBSD
 * Intelligent & automatic JVM detection (e.g. no need to have JAVA_HOME set)
 * Carefully researched, tested, and optimized methods for running daemons/services
    * Windows daemons installed as a service (32 and/or 64-bit daemons supported)
    * Linux/UNIX daemons can either use `exec` or use NOHUP, detach TTY properly,
      and do NOT spawn any sort of annoying helper/controller process
    * Execellent SystemD and SysV support
    * Mac OSX daemons integrate seamlessly with launchctl
    * All daemons can easily be run in non-daemon mode (to make debugging simpler)
    * All companion helper scripts are included to get the daemon to start
      at boot
 * Configurable methods supported for verifying a daemon started -- including useful
   debug output (e.g. if daemon fails to start, tail the log so the error is printed
   if an error is encountered).
 * Supports fixed or percentage-based min/max memory at JVM startup
 * Supports launching apps with retaining the working dir of the shell or setting
   the working directory to the home of app.
 * Sets the working directory of the app without annoyingly changing the working
   directory of the shell that launched the app (even on Windows).
 * Command-line arguments and/or system properties are seamlessly passed thru to
   the underlying Java app
 * Runtime debugging using simple LAUNCHER_DEBUG=1 env var before executing binary
   to see what's going on (e.g. how is the JVM found?)
 * Support for symlinking detected JVM as application name so that Linux/UNIX commands
   such as TOP/PS make identifying application easier.

### Usage

Compiles all launchers in src/main/launchers to target/stork (which will result
in target/stork/bin and target/stork/share dirs).

Command-line

    stork-launcher -o target/stork src/main/launchers

Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>stork-maven-plugin</artifactId>
            <version>3.0.0</version>
            <executions>
                <execution>
                    <id>stork-launcher</id>
                    <goals>
                        <goal>launcher</goal>
                    </goals>
                </execution>
            </executions> 
        </plugin>
        ...
    </plugins>
</build>
```

Gradle (to be released soon)

* task name: storkLauncher
```groovy
storkLaunchers {
    outputDirectory = new File("${project.buildDir}", "stork")
    inputFiles = ["${project.projectDir}/src/main/launchers".toString()]
    launcher {
        name =  "test1"
        displayName = "test1"
        domain = "com.fizzed.stork.test1"
        shortDescription = "desc"
        type = "DAEMON"
        platforms = ["LINUX","MAC_OSX"]
        workingDirMode = "APP_HOME"
        mainClass="class"
    }
    launcher {
            name =  "test2"
            displayName = "test2"
            domain = "com.fizzed.stork.test1"
            shortDescription = "desc"
            type = "DAEMON"
            platforms = ["LINUX","MAC_OSX"]
            workingDirMode = "APP_HOME"
            mainClass="class"
        }
}
```

To customize, the following properties are supported:

 - outputDirectory: The directory the launcher will compile/generate launchers
   to. Defaults to ${project.build.directory}/stork

 - inputFiles: An array of input directories or files to compile in a single
   invocation.  Defaults to ${basedir}/src/main/launchers

### Configuration file

```yaml
# Name of application (make sure it has no spaces)
name: "hello-console"

# Domain of application (e.g. your organization such as com.example)
domain: "com.fizzed.stork.sample"

# Display name of application (can have spaces)
display_name: "Hello Console App"

short_description: "Demo console app"

long_description: "Demo of console app for mfizz jtools launcher"

# Type of launcher (CONSOLE or DAEMON)
type: CONSOLE

# Java class to run
main_class: "com.fizzed.stork.sample.HelloConsole"

# Platform launchers to generate (WINDOWS, LINUX, MAC_OSX)
# Linux launcher is suitable for Bourne shells (e.g. Linux/BSD)
platforms: [ WINDOWS, LINUX, MAC_OSX ]

# Working directory for app
#  RETAIN will not change the working directory
#  APP_HOME will change the working directory to the home of the app
#    (where it was intalled) before running the main class
working_dir_mode: RETAIN

# Arguments for application (as though user typed them on command-line)
# These will be added immediately after the main class part of java command
# Users can either entirely override it at runtime with the environment variable
# APP_ARGS or append extra arguments with the EXTRA_APP_ARGS enviornment variable
# or by passing them in on the command-line too.
#app_args: "-c config.yml"

# Arguments to use with the java command (e.g. way to pass -D arguments)
# Users can either entirely override it at runtime with the environment variable
# JAVA_ARGS or append extra arguments with the EXTRA_JAVA_ARGS enviornment variable
# or by passing them in on the command-line too.
#java_args: "-Dtest=foo"

# Minimum version of java required (system will be searched for acceptable jvm)
# Defaults to Java 1.6.
#min_java_version: "1.6"

# Min/max fixed memory (measured in MB). Defaults to empty values which allows
# Java to use its own defaults.
#min_java_memory: 30
#max_java_memory: 256

# Min/max memory by percentage of system
#min_java_memory_pct: 10
#max_java_memory_pct: 20

# Try to create a symbolic link to java executable in <app_home>/run with
# the name of "<app_name>-java" so that commands like "ps" will make it
# easier to find your app. Defaults to false.
#symlink_java: true
```

## Overriding launcher environment variables

All launcher scripts are written to allow last-minute or per-environment 
replacement.  As of v2.7.0, let's say you needed to add a few more Java
system properties and wanted to execute a daemon launcher named "hellod".

    EXTRA_JAVA_ARGS="-Da=1 -Db=2" /opt/hellod/current/bin/hellod --run

If you run `hellod` as a daemon using SYSV or SystemD init scripts then stork
will load environment variables from `/etc/default/hellod`.  You could place
this value in there as well as others you need.  In `/etc/default/hellod`:

    APP_HOME=/opt/hello/current
    EXTRA_JAVA_ARGS="-Da=1 -Db=2"
    DB_PASSWORD=mypass

Stork's launcher scripts for daemons will load these environment variables
when starting.  For variables used by the launcher script (e.g. APP_HOME or
EXTRA_JAVA_ARGS), these are overrides.  For variables no used (e.g. DB_PASSWORD)
these are effectively passed through to the Java process.


## Stork assembly

Stages and assembles your application into a [canonical stork layout](docs/CANONICAL_LAYOUT.md).
The following are copied to target/stork/lib using the full groupId-artifactId-version naming
format:

 - Your project artifact (if its a jar)
 - Any additional "attached" runtime jar artifacts
 - Your runtime dependencies

Your project basedir conf/, bin/ and share/ directories are then copied to
target/stork (will overlay/overwrite any files currently in target/stork).
To include launchers as part of your assembly, you will need to include both
the assembly and one or more generate goals. Finally, the contents
of target/stork are tarballed into ${finalName}.tar.gz with an install prefix
of ${finalName} as the root directory of the tarball (so it unpacks correctly)

### Usage

Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>stork-maven-plugin</artifactId>
            <version>3.0.0</version>
            <executions>
                <execution>
                    <id>stork-assembly</id>
                    <goals>
                        <goal>assembly</goal>
                    </goals>
                </execution>
            </executions> 
        </plugin>
    </plugins>
</build>
```

Gradle (to be released soon)

* task name: storkAssembly
```groovy
storkAssembly {
    stageDirectory = new File("${project.buildDir}", "stork")
    outputFile = project.buildDir
}
```

What's nice is that target/stork still exists and you are free to directly
run anything in target/stork/bin -- since the launcher scripts correctly
pick up your relative dependencies.  You can quickly run your application
as though you had already deployed it to a remote system.

To customize, the following properties are supported:

 - stageDirectory: The directory where assembly contents will be staged to and
   tarballed from. Defaults to ${project.build.directory}/stork

 - outputDirectory: The directory the final tarball assembly will be output.
   Defaults to ${project.build.directory}

 - finalName: The final name of the assembly tarball -- as well as the name of
   the root directory contained within the tarball -- that will contain the 
   contents of stageDirectory. Defaults to ${project.build.finalName}


## Stork deploy

Utility for rapidly deploying a "versioned" install on one or more remote
Linux-based systems via SSH. Installs a stork-based assembly tarball into a
versioned directory structure on a remote system and handles restarting daemons
as needed.  The versioned directory structure allows rapid deployment with the
ability to revert to a previous version if needed.  Power users can combine with
[Blaze](https://github.com/fizzed/blaze) for even more advanced deploys.

### Usage

Command-line to traditional remote system

    stork-deploy --assembly target/myapp-1.0.0-SNAPSHOT.tar.gz ssh://host.example.com

Command-line to Vagrant

    stork-deploy --assembly target/myapp-1.0.0-SNAPSHOT.tar.gz vagrant+ssh://machine-name

### Overview

Since this a "SNAPSHOT" version, a timestamp would be generated (such as
20160401-121032 for April 1, 2016 12:10:32) and this application would be installed
to:

    /opt/myapp/v1.0.0-20160401-121032

A symlink will be created:

    /opt/myapp/current -> /opt/myapp/v1.0.0-20160401-121032

Since this application contains one daemon called "hello-server", the daemon
would be stopped (if it existed), the upgrade would occur, then the daemon would
be installed (if needed) and started back up.  The directories described above
in the canonical layout as (retained on upgrade) would be moved rather than
overwritten. That means during a fresh install, the bin/, lib/, conf/, and
share/ directories are installed.  On an upgrade install, the bin/, lib, and
share/ directories are installed, while conf/ and runtime dirs data/, log/, and
run/ directories are moved.

## Programmatic deploys using Blaze

You can combine Stork with [Blaze](https://github.com/fizzed/blaze) to make
automating your deployments even simpler.  You'll also never need to download
stork locally since Blaze will automatically fetch the dependency for you.

Download blaze:

    curl -o blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.16.0/blaze-lite-0.16.0.jar'

Create a `blaze.conf` file:

    blaze.dependencies = [
      "com.fizzed:stork-deploy:3.0.0"
    ]

Create a `blaze.java` file:

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fizzed.stork.deploy.Assembly;
import com.fizzed.stork.deploy.Assemblys;
import com.fizzed.stork.deploy.DeployOptions;
import com.fizzed.stork.deploy.Deployer;

public class blaze {

    private final Path archiveFile = Paths.get("target/hello-0.0.1-SNAPSHOT.tar.gz");

    @Task("Deploy assembly to staging env")
    public void deploy_stg() throws Exception {
        DeployOptions options = new DeployOptions()
            .user("hello")
            .group("hello");

        try (Assembly assembly = Assemblys.process(archiveFile)) {
            new Deployer().deploy(assembly, options, "ssh://app1");
            new Deployer().deploy(assembly, options, "ssh://app2");
        }
    }
}
```

Run it

    java -jar blaze.jar deploy_stg

## More examples

### stork-demo-hellod

[stork-demo/stork-demo-hellod](stork-demo/stork-demo-hellod)

To try this demo out, we use [Blaze](https://github.com/fizzed/blaze) for
scripting the build and execution process

    java -jar blaze.jar demo_hellod

### stork-demo-dropwizard

[stork-demo/stork-demo-dropwizard](stork-demo/stork-demo-dropwizard)

To try this demo out, we use [Blaze](https://github.com/fizzed/blaze) for
scripting the build and execution process

    java -jar blaze.jar demo_dropwizard

By default the server runs on port 8080 and you can then visit the sample in 
your browser @ http://localhost:8080/

## Development & contributing

Please see the [development guide](docs/DEV.md) for info on building, testing,
and eventually contributing to this project.

## License

Copyright (C) 2014+ Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
