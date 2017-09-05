Stork by Fizzed
===============

## Vagrant

Make sure you have at least Vagrant v1.9.8 installed as well as the extension pack. 
You can verify your version by running:

    vagrant -v

Stork interacts with various operating systems.  While many unit tests are designed
to run locally (and not in a virtual machine), the rest of them require 1 or
more virtual machines to run. Vagrant is used to help setup virtual machines running
various operating systems so we can run unit tests against them.  The unit tests
are designed to detect what's virtual machines are running and then run tests
against them -- so you only need to spin up what you'd like to test against. 
Let's run thru an "ubuntu14" example:

To spin up an Ubuntu 14.04 virtual machine:

    vagrant up ubuntu14

To run unit tests against a specific virtual machine:

    mvn test -Dhost=ubuntu14

To run just the launchers test:

     mvn -am -pl stork-launcher-test test-compile test -Dhost=ubuntu14 -DfailIfNoTests=false -Dtest=com.fizzed.stork.test.LauncherTest

To run tests against Windows (if on Linux or OSX):

    vagrant up windows10

Be patient as the image is ~6GB to download.  Once ready, you'll need to make
sure Java 8 is on it.  Open up VirtualBox, double click the vm, then open powershell

    choco install -y jdk8

Then run your tests

    mvn test -Dhost=windows10

If you change how the launchers work, please look at creating a unit test for
them in `stork-launcher-test`.  Validating your change doesn't break across
numerous operating systems is important.

## Windows

Its incredibly important your windows scripts have correct line endings or
the Windows cmd.exe interpreter will give you strange results.

    unix2dos stork-launcher/src/main/resources/com/fizzed/stork/launcher/windows/*
