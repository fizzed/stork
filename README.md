Stork by Fizzed
=======================================

## By

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))


## Overview

What comes after your Java/JVM build tool (Maven, SBT, Gradle, Ant, etc.) compiles your code?
Stork is a collection of utilities for optimizing your "after-build" workflow by filling in
the gap between your Java build system and eventual end-user app execution. Stork currently
provides one or more tools in the following main "after-build" activities:

 1. Launch - generate launcher scripts for one or more console and/or daemon JVM/Java
    apps that users will execute -- as well as the companion scripts to run those scripts
    across numerous operating systems (e.g. starting your daemon at boot).

 2. Assembly - package your application into a well-defined canonical application
    layout with a consistent location for your launcher scripts, jars, configuration files,
    etc.

 3. Deployment - rapidly deploy your assembly to one or more systems via a fabric-based
    installer.

By standardizing the layout of your Java-based application, you'll find both developers,
system administrators, and end-users are all on the same page with how to interact with
your apps.

You can choose to use all of the tools in your workflow or cherry-pick the ones that 
you need.


### Canonical Java application layout

All of Stork's tools need to know where to look for various files in order to function
in a standard way.  The following standard application layout is defined:

    <app_name>/	(
        bin/	(launcher scripts, overwrite on upgrade)
        lib/	(all jars, overwrite on upgrade)
        share/  (arch indep data for install/running/info; overwrite on upgrades)
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
to one specific host.  State information should generally remain valid after a
reboot, should not be logging output, and should not be spooled data.

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


## Installation

Download the stork tarball.  The "bin" directory in this tarball needs to be
added to your PATH environment variable.  Once available in your PATH, you can
execute any of the following utilities:

### stork-launcher-generate

Compiles a launcher config file into a launcher script.  Outputs compiled files
into a parent directory that will contain child directories in the canonical
application layout.

	stork-launcher-generate -i src/main/assembly/hello-server.yml -o target/stage

### stork-launcher-merge

Helper utility to merge launcher config files together by adding or overriding values
defined from the ordered list of input files.  Useful for using a base launcher
config file and only overriding values as needed.

	stork-launcher-merge -i src/main/assembly/hello-base.yml -i src/main/assembly/hello-server.yml -o target/hello-server-merged.yml
	stork-launcher-generate -i target/hello-server-merged.yml -o target/stage

### stork-play-assembly

Utility for assemblying a [PlayFramework](http://playframework.com) application into
a Stork-based assembly tarball.

	stork-play-assembly

### stork-fabric-deploy

Utility for rapidly deploying a "versioned" install on one or more remote Linux-based systems via SSH.
Uses Python and [Fabric](http://www.fabfile.org/) underneath.  Installs a stork-based assembly tarball
into a versioned directory structure on a remote system and handles restarting daemons as needed.  The
versioned directory structure allows rapid deployment with the ability to revert to a previous version
if needed.

	stork-fabric-deploy -H host1.example.com,host2.example.com --assembly target/hello-server-1.0.0-SNAPSHOT.tar.gz

Since this a "SNAPSHOT" version, a timestamp would be generated (such as 20141101121032 for Nov 1, 2014 12:10:32) and
this application would be installed to:

	/opt/hello-server/version-1.0.0-20141101121032

A symlink would also be created:

	/opt/hello-server/current -> /opt/hello-server/version-1.0.0-20141101121032

Since this application contains one daemon called "hello-server", the daemon would be stopped (if it existed), the
upgrade would occur, then the daemon would be installed (if needed) and started back up.  The directories described
above in the canonical layout as (retained on upgrade) would be moved rather than overwritten. That means during
a fresh install, the bin/, lib/, conf/, and share/ directories are installed.  On an upgrade install, the
bin/, lib, and share/ directories are installed, while conf/ and runtime dirs data/, log/, and run/ directories
are moved.

## Examples

### hello-server-dropwizard

Example project using Maven for building a simple Hello World daemon using the DropWizard framework.

The command-line version of stork-launcher-generate is integrated in the pom.xml via the maven-exec-plugin.
The packaging of the final tarball assembly is done with the standard maven-assembly-plugin.  To build the
project and tarball, just execute the following in [src/examples/hello-server-dropwizard](/src/examples/hello-server-dropwizard):

	mvn clean assembly:assembly

On success, the target/ directory will contain the final assembly tarball. This tarball is ready for
distribution or deployment using stork-fabric-deploy.

### hello-server-play

Example project using the [PlayFramework](http://playframework.com) for building a simple Hello World
daemon. The PlayFramework allows you to use a mix of Scala/Java for creating web applications.  Play uses
SBT underneath the hood, but they also define many special settings in SBT for building their applications.
The stork-play-assembly tool automates using the play build system to structure a final assembly tarball
that meets the stork canonical standards. It's also a great example of how any JVM-based application 
can ultimately be packaged into the stork layout.  To build the project, just execute the following
in [src/examples/hello-server-play](/src/examples/hello-server-play):

	stork-play-assembly

On success, the target/ directory will contain the final assembly tarball.  This tarball is ready for
distribution or deployment using stork-fabric-deploy.


## Launcher

Collection of utilities for generating native launchers for Java-based applications
across Windows, Linux, Mac OSX, and many other UNIX-like systems (any NIX with a
JVM and bourne shell support). 

You simply create a YAML-based config file (that you can check-in to
source control) and then you compile/generate it into one or more launchers. These
launchers can then be distributed with your final tarball/assembly/package so 
that your app looks like a native compiled executable.

### Development workflow

Integrate "stork-launcher-generate" into your build workflow to generate the launcher
scripts while you also compile your JVM bytecode classes.

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


## License

Copyright (C) 2014 Joe Lauer / Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.

