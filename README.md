Java Native Executable Library
==============================

### Contributors

 - [Mfizz, Inc.](http://mfizz.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Overview

Utility for generating native launchers for Java-based applications across Windows,
Linux, and Mac OSX. Unlike other "service wrappers", this utility can create
simple console application

While other Java service wrappers attempt to handle re-spawning via a secondary
controller process, what about relatively simple console apps / daemons or
an interest in using better tools for handling re-spawning like monit.

### Canonical layout of application folder

    <app_home>
     - bin
     - lib
     - conf
     - share
     - data


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
 
 * Supports launching apps with retaining the working dir of the shell or setting
   the working directory to the home of app.
 * Sets the working directory of the app without annoyingly changing the working
   directory of the shell that launched the app (even on Windows).
 * Command-line arguments are passed thru to underlying java app




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