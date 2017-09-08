Stork by Fizzed
===============

## Overview

Stork heavily interacts with various operating systems.  While many unit tests
are designed to run locally (and not in a virtual machine), to truly do development
with Stork you'll want to verify what you changed works across a number of
operating systems.

## Vagrant

Make sure you have at least Vagrant v1.9.8 installed as well as the extension pack. 
You can verify your version by running:

    vagrant -v

Vagrant is used to help setup virtual machines running various operating systems
so we can run unit tests against them.  The unit tests are designed to detect what's
virtual machines are running and then run tests against them -- so you only need to
spin up what you'd like to test against. Let's run thru an "ubuntu14" example:

To spin up an Ubuntu 14.04 virtual machine:

    vagrant up ubuntu14

## Running Unit Tests

To run all unit tests against a specific virtual machine:

    mvn test -Dhost=ubuntu14

To test only the stork-launcher module on that host:

    mvn -am -pl stork-launcher test -DfailIfNoTests=false -Dtest=com.fizzed.stork.launcher.*Test -Dhost=ubuntu14 

To test only the stork-deploy module on that host:

    mvn -am -pl stork-deploy test -DfailIfNoTests=false -Dtest=com.fizzed.stork.deploy.*Test -Dhost=ubuntu14 

To run your tests locally, just use the host of `local`:

    mvn test -Dhost=local

## Testing for Windows on Linux/Mac

To run tests against Windows (if on Linux or OSX):

    vagrant up windows10

Be patient as the image is ~6GB to download.  Once ready, you'll need to make
sure Java 8 is on it.  We didn't have time to make the vagrant install do all
the prep, so there's a couple manual steps.  Open up VirtualBox, double click
the vm, then open powershell:

    choco install -y jdk8

Then you can run any of the tests above against the host `windows10`:

    mvn test -Dhost=windows10

NOTE: its incredibly important your windows scripts have correct line endings or
the Windows cmd.exe interpreter will give you strange results.

    unix2dos stork-launcher/src/main/resources/com/fizzed/stork/launcher/windows/*