Stork by Fizzed
=======================================

[![Build Status](https://travis-ci.org/fizzed/stork.svg?branch=master)](https://travis-ci.org/fizzed/stork)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/stork/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/stork)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Overview

So you've engineered that amazing Java-based application.  Then what?  Distributing
it or getting it into production is your new problem.  Fat/uber jar? Vagrant?
Docker? Traditional deploy?

Stork is a collection of utilities for optimizing your "after-build" workflow by
filling in the gap between your Java build system and execution.  Stork supports
running your app anywhere (Docker, Vagrant, traditional systems).

 - [stork-launcher](#stork-launcher) will generate well-tested, rock solid, secure launcher scripts
   from a yaml configuration file for either console or daemon/service JVM apps.
   The generated launchers will run your app the same way regardless of whether
   running within Docker, Vagrant, or a traditional system.

 - [stork-assembly](#stork-assembly) will assemble your JVM app into a standard,
   well-defined [canonical layout](docs/CANONICAL_LAYOUT.md) as a tarball ready
   for distribution or deployment.

 - [stork-deploy](#stork-deploy) will rapidly and securely deploy your assembly
   via SSH into a versioned directory structure to Vagrant or a traditional system.
   It will handle restarting daemons, use strict user/group permissions, and verify
   the deploy worked.  Power users can combine with [Blaze](https://github.com/fizzed/blaze)
   for even more advanced deploys.

[Using Stork to deploy a production Ninja Framework app](http://fizzed.com/blog/2015/01/using-stork-deploy-production-ninja-framework-app)

## Example

[stork-demo-hellod](stork-demo/stork-demo-hellod) is an example Maven project
for a simple Java web application.  It demos the `stork-launcher`
and `stork-assembly` utilities and produces a tarball assembly that can be
deployed using `stork-deploy`.  To generate the launchers and assembly:

    mvn package

This will generate all launchers, prep the assembly in `target/stork`, and
tarball it up to `target/stork-demo-hellod-X.X.X.tar.gz` (X.X.X is the version
of your project).  You can quickly try it out:

    target/stork/bin/stork-demo-hellod --run

Or you can deploy it via SSH using `stork-deploy`:

    stork-deploy --assembly target/stork-demo-hellod-X.X.X.tar.gz ssh://host.example.com

## Usage

### Command-line

https://github.com/fizzed/stork/releases/download/v2.4.0/stork-2.4.0.tar.gz

### Maven plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>stork-maven-plugin</artifactId>
            <version>2.4.0</version>
            <!-- configuration / execution (see below) -->
        </plugin>
    </plugins>
</build>
```

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
so  that your app looks like a native compiled executable.

### Features

 * Generate *secure* launcher scripts for either console or daemon/service JVM apps
 * Heavily unit tested across all major operating systems
    * Windows XP+ (32-bit and 64-bit)
    * Linux (Ubuntu, Debian, Redhat) (32-bit and 64-bit)
    * Mac OSX (32-bit and 64-bit)
    * FreeBSD
    * OpenBSD
    * NetBSD
 * Intelligent & automatic JVM detection (e.g. no need to have JAVA_HOME set)
 * Carefully researched, tested, and optimized methods for running daemons/services
    * Windows daemons installed as a service (32 and/or 64-bit daemons supported)
    * Linux/UNIX daemons use NOHUP, detach TTY properly, and do NOT spawn any
      sort of annoying helper/controller process
    * Mac OSX daemons integrate seamlessly with launchctl
    * All daemons can easily be run in non-daemon mode
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
 * Command-line arguments are seamlessly passed thru to underlying Java app
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
            <version>2.4.0</version>
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
#app_args: "-c config.yml"

# Arguments to use with the java command (e.g. way to pass -D arguments)
#java_args: "-Dtest=foo"

# Minimum version of java required (system will be searched for acceptable jvm)
min_java_version: "1.6"

# Min/max fixed memory (measured in MB)
min_java_memory: 30
max_java_memory: 256

# Min/max memory by percentage of system
#min_java_memory_pct: 10
#max_java_memory_pct: 20

# Try to create a symbolic link to java executable in <app_home>/run with
# the name of "<app_name>-java" so that commands like "ps" will make it
# easier to find your app
symlink_java: true
```

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
            <version>2.4.0</version>
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
