Stork by Fizzed
=======================================

## Overview

Primarily an example of launcher configs -- see src/main/launchers directory for
example CONSOLE and DAEMON configs.

The maven pom used in this example is not recommended for your own app -- it 
happens to directly call the underlying fizzed-stork-launcher library -- rather
than the maven plugin.  Why?  Because this project is the main testing harness
for that code.  Take a look at the hello-server-dropwizard example for a better
Maven example.

