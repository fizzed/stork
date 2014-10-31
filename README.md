Stork by Fizzed
=======================================

### Contributors

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Overview

Utility for generating native launchers for Java-based applications across Windows,
Linux, Mac OSX, and many other UNIX-like systems (any NIX with a JVM and bourne shell
support). Unlike other "service wrappers" or launcher frameworks, this utility can
create launchers for either console and/or daemon apps.

You simply create a YAML-based descriptor/config file (that you can check-in to
source control) and then you compile/generate it into one or more launchers. These
launchers can then be distributed with your final tarball/assembly/package so 
that your app looks like a native compiled executable.

### Features

 * Intelligent & automatic JVM detection (including version requirements
   such as requiring Java 1.8)
 * Creating launchers for one or more application types:
    * console
    * daemon
 * Support for:
    * Windows XP+ (32-bit and 64-bit)
    * Linux (32-bit and 64-bit)
    * Mac OSX (32-bit and 64-bit)
    * FreeBSD
    * OpenBSD
 * Carefully researched and optimized daemonizing methods for each target OS:
    * Windows daemons installed as a service (32/64-bit daemons supported)
    * Linux/UNIX daemons use NOHUP and includes init.d startup scripts
    * Mac OSX daemons use LaunchD
 * Supports fixed or %age min/max memory at JVM startup 
 * Supports launching apps with retaining the working dir of the shell or setting
   the working directory to the home of app.
 * Sets the working directory of the app without annoyingly changing the working
   directory of the shell that launched the app (even on Windows).
 * Command-line arguments are passed thru to underlying Java app
 * Handles spaces in file paths
 * Apps can use "canonical" filesystem layout defined below or customize as needed.
 * Runtime debugging using simple LAUNCHER_DEBUG=1 env var before executing binary
   to see what's going on (e.g. how is the JVM found?)
 * Support for symlinking detected JVM as application name so that Linux/UNIX commands
   such as TOP/PS make identifying application easier.

### Samples

#### Console App (Hello World)

A basic "Hello World" console app launcher config: src/test/resources/hello-console.yml

#### Daemon App (Hello World)

A basic "Hello World" daemon app launcher config: src/test/resources/hello-daemon.yml

#### Console Apps (this project!)

This project uses itself to compile the launcher scripts for the two native Java-based
applications it distributes.  These scripts are useful as examples as well.

src/main/resources/jtools-launcher-generate.yml
src/main/resources/jtools-launcher-merge.yml

### Install / Generate a launcher

Add <fizzed_stork_install_dir>/bin to your PATH so that you can execute
"stork-launcher-generate".  Creating a launcher is then as easy as:

stork-launcher-generate -i <input_config_file> -o <output_dir>

### Development

Since this app creates launchers for other Java apps, a somewhat unusual build system
was required for testing & assembly during development and distribution.  There is
an Ant-based build.xml script which in turn creates maven commands for compiling, etc.
So you'll need both ant and maven available if you plan on building from source. The
assembled distribution, however, uses "console" launcher scripts generated from this
project -- so this application can easily run on Windows, Linux, Mac OSX, etc.

### License

Copyright (C) 2014 Joe Lauer / Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.

### Canonical application layout

The "launchers" need to know where to look for various files in order to bootstrap
the Java command to start/stop/execute the Java application.  The following standard
application layout is used as a default.

A standard Java app has the following layout:

    <app_name>/	(
        bin/	(launcher scripts, overwrite on upgrade)
        lib/	(all jars, overwrite on upgrade)
        share/  (data/files for install/running/info; overwrite on upgrades)
	    conf/	(config files; retain on upgrade)
        data/   (not included in assembly/install; retain on upgrade)
        log/    (not included in assembly/install; retain on upgrade)
        run/    (not included in assembly/install; retain on upgrade)

#### bin/ (executables)

For all read-only executables.  These are the binaries the user will execute.
Assume the user may place these in a read-only filesystem.

Examples include batch files or shell scripts to start your Java app.

#### lib/ (libraries)

All shared files and libraries required for running the application(s).

Examples include jar files containing compiled Java classes.

#### conf/ (configuration data)

All configuration files for the application(s). Any files in this directory
need to be carefully examined during an upgrade -- since the user may have
edited the config for their specific system.

#### share/ (architecture-independent data)

For all read-only architecture independent data files.

Examples would include sql scripts to setup databases; linux/unix init.d scripts, or
documentation.

#### data/ (variable state information)

State information is data that programs modify while they run, and that pertains
to one specific host.  State information should generally remain valid after a
reboot, should not be logging output, and should not be spooled data.

Files in this directory should be retained between upgrades.

Examples would include an application's database.  On Linux/UNIX, this could be
symlinked to /var/data/<app_name>.

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

### Notes for target operating systems

#### Windows

Daemonizers considered:

	http://jslwin.sourceforge.net/
	https://github.com/kohsuke/winsw

#### Mac OSX

References:
    http://couchdb.readthedocs.org/en/latest/install/mac.html
    http://vincent.bernat.im/en/blog/2013-autoconf-osx-packaging.html
    https://github.com/jenkinsci/jenkins/tree/master/osx

Standard install location for daemons:
    /Library/Application Support/<app_display_name>

    sudo mkdir -p /Library/Application\ Support/Hello\ Server
    sudo cp -R target/sample/* /Library/Application\ Support/Hello\ Server/

Standard install location for launchd startup script:
    sudo cp target/sample/share/osx/com.example.hello-daemon.plist /Library/LaunchDaemons/

/Library/LaunchDaemons System-wide daemons provided by the administrator.
started and available at boot (even if no user logged in yet).
    
To load (and start) daemon. Required in order to trigger on next boot.
    sudo launchctl load -F /Library/LaunchDaemons/com.example.hello-daemon.plist

To see what is going on (should have a PID value):
    sudo launchctl list com.example.hello-daemon

To unload (stop) daemon:
    sudo launchctl unload /Library/LaunchDaemons/com.example.hello-daemon.plist

#### Resources

Jenkins project:
	https://github.com/sbt/sbt-native-packager/tree/master/src/main/resources/com/typesafe/sbt/packager/archetypes

	https://gist.github.com/djangofan/1445440

