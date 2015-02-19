Stork by Fizzed [![Build Status](https://travis-ci.org/fizzed/java-stork.svg?branch=master)](https://travis-ci.org/fizzed/java-stork)
=======================================

 - [Fizzed, Inc.](http://fizzed.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

## Versions

 - Stork Command-Line: https://github.com/fizzed/java-stork/releases/download/v1.2.2/fizzed-stork-1.2.2.tar.gz
 - Stork Maven Plugin: see section below (via Maven Central)
 - Stork SBT Plugin for Play Framework: see section below (via Maven Central)

## 2-Minute Drill

Demos are sometimes faster than reading this doc.  Here are some good blog posts
on using Stork:

[Using Stork to deploy a production Ninja Framework app](http://fizzed.com/blog/2015/01/using-stork-deploy-production-ninja-framework-app)


## Overview

What comes after your Java/JVM build tool (Maven, SBT, Gradle, Ant, etc.) compiles
your code? There are many tools for building your project, but
then what? A tarball? A debian/rpm package? An Uber/Fat Jar? Some sort of installer?
Here's the problem with those approaches. First, they are tightly
coupled with your build tool -- which makes it tough to switch between projects
that use different build tools. Second, they lack flexibility in case you need
to use your final assembly in an unexpected way (rapid deploy to staging, or deliver
an installable package to a customer, etc).  Third, what do you do with the 
non-Java parts of your app such as configuration files? What if you want to
distribute a command-line console app along with your daemon? What if you have
little control over the end user system (e.g. not sure where they may install
Java)?  What happens on an upgrade?

Stork is a collection of utilities for optimizing your "after-build" workflow by
filling in the gap between your Java build system and eventual end-user app
execution. Stork functions seamlessly with whatever build system you prefer and
provides one or more tools in the following main "after-build" activities:

 1. Launch - generate well-tested, rock solid, reliable launcher scripts for one
    or more console and/or daemon JVM/Java apps that users will execute -- as
    well as the companion scripts to run those scripts across numerous operating
    systems (e.g. starting your daemon at boot, running as a true service on
    Windows).  This part of Stork is a stand-alone Java library that isn't directly
    tied to any single build tool.  Thus, you are not locking yourself into a
    workflow that relies so much on your build tool.

 2. Assembly - package your application into a well-defined, canonical application
    layout with a consistent location for your launcher scripts, jars, config
    files, and miscellaneous distributable dependencies and docs.  You can 
    either use some of the provided build tool plugins or use a standard
    "assembly" approach in your build tool to meet the defined layout guidelines.
    This assembly is a universal package that is ready for install and deployment
    on any operating system.  As long as your assembly meets the canonical layout
    any Stork deployment tool will be able to handle it.

 3. Deployment - rapidly deploy your assembly to one or more systems via a fabric-based
    installer.  Or in future versions, convert your assembly tarball into an
    operating system specific installer (e.g. a .dmg for OSX or .msi/.exe for
    Windows).  Think of your assembly as a well defined tarball -- where 
    tools can easily be created to distribute your app in various ways.  Hopefully,
    this is where the open source community can help!

By standardizing the layout of your Java-based application, you will find developers,
system administrators, and end-users are all on the same page with how to interact with
your JVM-based apps.


### Why are Uber/Fat jars not recommended for *most* applications?

An Uber/Fat jar is a jar that has all dependencies merged into it. There
are several reasons why Stork suggests avoiding this approach. First, if you
want to "rsync" your deployment for rapidly deploying an upgrade -- in most cases
the dependencies do not change much and usually eat up most of the disk space.
By not using a fat jar, then only the jars that have changed need to be 
transferred over the network and deployed -- resulting in a fast upgrade.

Second, you will lose the ability to quickly scan the "lib" directory and see
exactly what dependencies (including the version) are currently in use.

Third, if you need to offer users multiple entry points to your app (e.g. 
various console applications or daemons) and you are likely using an executable
fat/uber jar -- you are going to have multiple jars that have duplicates of
all dependencies.   That can quickly lead to large disk space usage even for
small apps.

Fourth, many libraries include resources within their .jar that may or may not
work correctly when re-packaged into another .jar.  Avoiding that entirely
is a good thing.


## The Stork Launcher

Utility for generating native launchers for Java-based applications
across Windows, Linux, Mac OSX, and many other UNIX-like systems (any NIX with a
JVM and bourne shell support). 

You simply create a YAML-based config file (that you can check-in to
source control) and then you compile/generate it into one or more launchers. These
launchers can then be distributed with your final tarball/assembly/package so 
that your app looks like a native compiled executable.

### Development workflow

Use it via one of the build tool plugins or simply call "stork-generate" from the
command-line version.  Simply include Stork as part of your build workflow to
generate the launcher scripts while you also compile your JVM bytecode classes.

### Features

 * Create launchers for one or more CONSOLE or DAEMON applications that will be
   included in your assembly. Launchers feel like a natively compiled app.

 * Console and daemon launchers are supported across all popular operating systems:

    * Windows XP+ (32-bit and 64-bit)
    * Linux (32-bit and 64-bit)
    * Mac OSX (32-bit and 64-bit)
    * FreeBSD
    * OpenBSD

 * Intelligent & automatic JVM detection (e.g. no need to have JAVA_HOME set)
    
 * Carefully researched, tested, and optimized daemonizing methods for each OS:

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

### Sample Launcher Config

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


### Canonical/Conventional Java application layout

All of Stork's tools need to know where to look for various files in order to
function in a standard way.  Stork uses the following conventional app layout:

    <app_name>/	(
        bin/	(launcher scripts, overwrite on upgrade)
        lib/	(all jars, overwrite on upgrade, fat jars not recommended)
        share/  (arch indep data for install/running/info; overwrite on upgrade)
        conf/	(config files; retain on upgrade)
        data/   (not included in assembly/install; retain on upgrade)
        log/    (not included in assembly/install; retain on upgrade)
        run/    (not included in assembly/install; retain on upgrade)

#### bin/ (executables)

For all read-only executables.  These are the binaries the user will execute to
run your console application or start/stop your daemon.  The executables should
look and feel like a native application.

Examples include batch files or shell scripts to start your Java app. Assume
file permissions of 0755.

#### lib/ (libraries)

All shared files and libraries required for running the application(s).

Examples include jar files containing compiled Java classes. Assume file permissions
of 0644.

#### conf/ (configuration data)

All configuration files for the application(s). Any files in this directory
need to be carefully examined during an upgrade -- since the user may have
edited the config for their specific system.  Assume file permissions of 0644.

#### share/ (architecture-independent data)

For all read-only architecture independent data files.

Examples would include sql scripts to setup databases; linux/unix init.d scripts, or
documentation. Assume this data will be overwritten on application upgrades.
Assume file permissions of 0644.

#### data/ (variable state information)

State information is data that programs modify while they run, and that pertains
to one specific host.  State information remains valid after a reboot, should
not be logging output, and should not be spooled data.

Files in this directory should be retained between upgrades.

Examples would include an application's database.  On Linux/UNIX, this could/may be
symlinked to /var/data/<app_name>.  Assume file permissions of 0644.

#### log/ (logfiles)

Logfiles for application (startup, runtime, etc.). It must be acceptable to
truncate or delete files in this directory w/o affecting the application on
its next invocation.

Files in this directory may be retained between upgrades, but assume they will
be deleted.  On Linux/UNIX, this could be symlinked to /var/log/<app_name>.

#### run/ (run-time variable data)

This directory contains system information data describing the system since it
was booted. Files under this directory may/will be cleared (removed or truncated
as appropriate) at the beginning of the boot process. On some versions of linux,
/var/run is mounted as a temporary file system.

Examples would include an application's process id (pid) file or named sockets.
On Linux/UNIX, this could be symlinked to /var/run/<app_name>.


## Usage

Stork provides a combination of plugins to various build systems like Maven
and SBT as well as command-line apps for similiar and additional utilities.
However, even if a plugin isn't provided, you can always just call the 
command-line version, code a plugin (and submit a pull request to add it),
or tap into the main core library.

## Stork Maven Plugin

Example Maven project: examples/hello-server-dropwizard 

Plugin provides two goals -- compiling launchers and assembling into a tarball.
Using the Maven plugin does not require installation of the stork command-line
apps -- since it is deployed to Maven central and will be downloaded during
a build.

### Goal: generate

Compiles all launchers in src/main/launchers to target/stork (which will result
in target/stork/bin and target/stork/share dirs).

To use add the following to your POM:

    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.fizzed</groupId>
                <artifactId>fizzed-stork-maven-plugin</artifactId>
                <version>1.2.2</version>
                <executions>
                    <execution>
                        <id>generate-stork-launchers</id>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions> 
            </plugin>
            ...
        </plugins>
    </build>

To customize, the following properties are supported:

 - outputDirectory: The directory the launcher will compile/generate launchers
   to. Defaults to ${project.build.directory}/stork

 - inputFiles: An array of input directories or files to compile in a single
   invocation.  Defaults to ${basedir}/src/main/launchers

### Goal: assembly

Stages and assembles your application into a canonical Stork layout. The following
are copied to target/stork/lib using the full groupId-artifactId-version naming
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

    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.fizzed</groupId>
                <artifactId>fizzed-stork-maven-plugin</artifactId>
                <version>1.2.2</version>
                <executions>
                    <execution>
                        <id>generate-stork-assembly</id>
                        <goals>
                            <goal>assembly</goal>
                        </goals>
                    </execution>
                </executions> 
            </plugin>
            ...
        </plugins>
    </build>

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


## Stork Command Line

Download the stork tarball.  The "bin" directory in this tarball needs to be
added to your PATH environment variable.  Once available in your PATH, you can
execute any of the following utilities:

### stork-generate

Compiles a launcher config file into a launcher script.  Outputs compiled files
into a parent directory that will contain child directories in the canonical
application layout.

    stork-generate -i src/main/launchers/hello-server.yml -o target/stork

### stork-merge

Helper utility to merge launcher config files together by adding or overriding values
defined from the ordered list of input files.  Useful for using a base launcher
config file and only overriding values as needed.

    stork-merge -i src/main/launchers/hello-base.yml -i src/main/launchers/hello-server.yml -o target/hello-server-merged.yml
    stork-generate -i target/hello-server-merged.yml -o target/stork

### stork-fabric-deploy

Utility for rapidly deploying a "versioned" install on one or more remote
Linux-based systems via SSH. Uses Python and [Fabric](http://www.fabfile.org/)
underneath.  Installs a stork-based assembly tarball into a versioned directory
structure on a remote system and handles restarting daemons as needed.  The
versioned directory structure allows rapid deployment with the ability to revert
to a previous version if needed.

    stork-fabric-deploy -H host1.example.com,host2.example.com --assembly target/hello-server-1.0.0-SNAPSHOT.tar.gz

Since this a "SNAPSHOT" version, a timestamp would be generated (such as
20141101121032 for Nov 1, 2014 12:10:32) and this application would be installed
to:

    /opt/hello-server/version-1.0.0-20141101121032

A symlink would also be created:

    /opt/hello-server/current -> /opt/hello-server/version-1.0.0-20141101121032

Since this application contains one daemon called "hello-server", the daemon
would be stopped (if it existed), the upgrade would occur, then the daemon would
be installed (if needed) and started back up.  The directories described above
in the canonical layout as (retained on upgrade) would be moved rather than
overwritten. That means during a fresh install, the bin/, lib/, conf/, and
share/ directories are installed.  On an upgrade install, the bin/, lib, and
share/ directories are installed, while conf/ and runtime dirs data/, log/, and
run/ directories are moved.

## Stork SBT Plugin for PlayFramework

The PlayFramework is a popular Scala/Java framework that uses SBT underneath
for its build system.  Stork has tight integration with PlayFramework via its
plugin.

NOTE: You must be using PlayFramework version >= 2.3.6 and SBT 0.13.5

This plugin takes advantage of being an AutoPlugin which was introduced in
PlayFramework 2.3.6 and SBT 0.13.5 -- which makes auto importing of settings and
activation of plugins a piece of cake.

Example Play project: examples/hello-server-play

Play is great, but there are some strange choices (IMHO) regarding config files
during startup and where it finds them.  By default, Play will use your conf/
directory during development, but then add them as resources to your compiled
JARs for deployment.  Nothing like hard-coded configs, eh?  The only way to
modify a config in production is to create your own new conf/ directory, 
extract the configs from the compiled JARS, and then add a Java system property
before you run Play that tells it the new config to run at start.  It's logging
levels for production also are less than desired (IMHO).  Therefore, this plugin
will add two system properties by default to your startup -- to force play to
read the files from conf/ in production:

    -Dconfig.file=conf/application.conf -Dlogger.file=conf/logger.xml

Since Stork's canonical app layout always includes a conf directory -- this plugin
will copy whatever is in your conf/ directory to your assembly tarball.  The
ones Play hard-coded in your compiled jars still exists, but Play will at least
be forced to load the conf/application.conf from the conf/ directory vs. the
one compiled into your .jar.  Remember that this plugin also opts to start your
Java app with it's bootstrap main class vs. Play's default NettyServer.  It's 
a little easier to understand what happens by seeing the final java command used
to start your app:

    <java, classpath, etc. and then> -Xrs -Djava.net.preferIPv4Stack=true -Dlauncher.main=play.core.server.NettyServer -Dconfig.file=conf/application.conf -Dlauncher.bootstrap=conf/stork-bootstrap.conf -Dlogger.file=conf/logger.xml com.fizzed.stork.bootstrap.PlayBootstrap

These defaults can customized in your app to something else.  Simply tweak the
SettingKeys to something else or set the 
"java_args" property in your conf/stork-launcher.yml file to whatever you'd like
the line to be. There are four SettingKeys to customize what the plugin does:

 - storkPlayConf: Config file to load at runtime to configure play app. Defaults
        to conf/application.conf (play's default)
 - storkPlayLauncherConf: Launcher config file to use to override this plugins
        default launcher config.  Defaults to conf/stork-launcher.yml
 - storkPlayBootstrapConf: The launcher bootstrap config file to use at runtime
        to set system properties before starting play. Useful for setting the
        http.port play will bind to by default. Defaults to conf/stork-bootstrap.conf
 - storkAssemblyStageDir: The directory this plugin will stage the stork
        compliant assembly. Defaults to target/stork

The plugin affects your application in two main ways.  First, since Play only
has a well-defined entry point to your application, the plugin includes a 
well-defined launcher config file that is suitable for your app.  So you don't
need to even create one (unless you want to customize something). The plugin
allows an override to be placed in conf/stork-launcher.yml.

Second, the plugin supplies an interesting Bootstrap class that becomes the
new entry point to your app.  The Bootstrap is pretty simple.  It reads in
an optional config file that defines system property values then calls the 
normal entry point to a Play app.  This permits you to set the http.port the
server runs on in either development or production and maintain it in a config
file.  The config file is optional, but if you'd like to use it just supply
a conf/stork-bootstrap.conf file with a system property on each line:

    http.port=9001

To enable in your Play project add the following to project/plugins.sbt:

    // stork play plugin
    addSbtPlugin("com.fizzed" % "fizzed-stork-sbt-play-plugin" % "1.2.2")

The plugin extends SBT AutoPlugin which auto enables settings in your project
so you will not need to add anything else to your project to pick up the plugin.

The plugin implementation relies on the default Play "stage" task
to figure out what jars to copy.  As avid users of Play over the years, the
platform has changed how it stages jars between versions and tapping into 
the existing task is more stable.  The example project will be the easiest
way to see how it works.  Thus, to assembe your play project run the following:

    activator stage stork-assembly

This will result in a target/stork directory containing the staged project and
target/ will contain a tarball of the assembly. 


## Examples

### hello-server-dropwizard

Example project using Stork Maven Plugin for building a simple Hello World daemon
using the DropWizard framework.

To build the project and assembly tarball, just execute the following in 
src/examples/hello-server-dropwizard:

    mvn package
    target/stork/bin/hello-server-dropwizard --run

By default the server runs on port 8080 and you can then visit the sample in 
your browser @ http://localhost:8080/

The target/ directory will contain the final assembly tarball. This tarball is
ready for distribution or deployment using stork-fabric-deploy.

### hello-server-play

Example project using the Stork SBT Plugin for the [PlayFramework](http://playframework.com).
The PlayFramework allows you to use a mix of Scala/Java for creating web
applications.  Play uses SBT underneath the hood, but they also define many
special settings in SBT for building their applications.

To build the project and use this plugin, you'll need to run the following.

    activator stage stork-assembly
    target/stork/bin/hello-server-play --run

On success, the target/ directory will also contain the final assembly tarball.
This tarball is ready for distribution or deployment using stork-fabric-deploy.


## License

Copyright (C) 2014 Joe Lauer / Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
