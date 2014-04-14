Java Native Executable Library
==============================

### Contributors

 - [Mfizz, Inc.](http://mfizz.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Overview

Utility for generating native launchers for Java-based applications across Windows,
Linux, Mac OSX, and many other UNIX-like systems w/ bourne shell support. Unlike
other "service wrappers" or launcher frameworks, this utility can create launchers
for either console or daemon apps.  Just create a config file describing your
launcher and then run the tool to "compile" it into various launchers.

While other Java service wrappers attempt to handle re-spawning via a secondary
controller process, what about relatively simple console apps / daemons or
an interest in using better tools for handling re-spawning like monit.

### Canonical application layout

A standard Java app has the following layout:

    <app_name>/
        bin/
        lib/
        conf/
        share/   (architecture independent read-only data optionally included during install)
        data/    (not present at install time; ignored during upgrade)
        log/     (not present at install time; ignored during upgrade)
        run/     (not present at install time; ignored during upgrade)

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

Examples would include sql scripts to setup databases; linux setup scripts, or
documentation.

#### data/ (variable state information)

State information is data that programs modify while they run, and that pertains
to one specific host.  State information should generally remain valid after a
reboot, should not be logging output, and should not be spooled data.

Files in this directory should be retained between upgrades.

Examples would include an application's database.

#### log/ (logfiles)

Logfiles for application (startup, runtime, etc.). It must be acceptable to
truncate or delete files in this directory w/o affecting the application on
its next invocation.

Files in this directory may be retained between upgrades, but assume they will
be deleted.

#### run/ (run-time variable data)

This directory contains system information data describing the system since it
was booted. Files under this directory may/will be cleared (removed or truncated
as appropriate) at the beginning of the boot process. On some versions of linux,
/var/run is mounted as a temporary file system.

Examples would include an application's process id (pid) file or named sockets.




For Linux/UNIX, good reference of standard filesystem:

    http://www.pathname.com/fhs/pub/fhs-2.3.html
    
    /opt/<app_name>/
        bin/
        lib/
        conf/
        share/
        data    -> soft link to /var/lib/<app_name>
        log     -> soft link to /var/log/<app_name>
        run     -> soft link to /var/run/<app_name>

LAUNCHER_DEBUG=1 bin/app_name


Features

 * Creating launchers for app types:
    * console
    * daemon
 * Support for:
    * Windows XP+ (32-bit and 64-bit)
    * Linux (32-bit and 64-bit)
    * Mac OSX (32-bit and 64-bit)
    * FreeBSD
    * OpenBSD
 
 * Supports launching apps with retaining the working dir of the shell or setting
   the working directory to the home of app.
 * Sets the working directory of the app without annoyingly changing the working
   directory of the shell that launched the app (even on Windows).
 * Command-line arguments are passed thru to underlying java app
 * Handles spaces in file paths



#### Mac OSX

Good references:
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




#### Windows

http://jslwin.sourceforge.net/

https://github.com/kohsuke/winsw


#### Resources

Jenkins project

https://github.com/sbt/sbt-native-packager/tree/master/src/main/resources/com/typesafe/sbt/packager/archetypes

https://gist.github.com/djangofan/1445440



### License

Copyright (C) 2014 Joe Lauer / Mfizz, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.