Stork by Fizzed (Launcher Sample)
=======================================

### Contributors

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Overview

Sample "hello world" console and daemon apps.

### Running samples

cd <install_root>

bin/hello-console

bin/hello-daemon

### Using in your own project

This project is a special case where there is an Ant build script which runs
Maven commands underneath.  However, on your own system, once "fizzed-stork"
is installed in your PATH, you can simply add "stork-launcher-generate" in the
maven exec plugin during the package phase.

### Development

ant hello-console
ant hello-daemon

### Distribution

ant assembly

Copy target/*.tar.gz to final resting place
