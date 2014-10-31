Stork by Fizzed
=======================================

### Overview

Stork example project demonstrating the following:

 - Maven build system using command-line version of stork-launcher-generate
 - Maven exec plugin compiles launchers during "package" phase
 - DropWizard-based HTTP server
 - Maven assembly plugin packages everything into canonical java app layout

To run this example:

 - Install stork so its available on your PATH
 - "make server" to run server via maven
 - "make assembly" to package into tarball in target/
 - Unpackage final tarball then do "bin/hello-server" to try out launcher
