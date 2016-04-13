Stork by Fizzed
=======================================

## Vagrant

Stork interacts heavily with other systems, so Vagrant is used to help create
a local testing environment for many different operating systems.  The unit
tests are designed to work with only a subset of live systems, so you don't need
to spin everything up.

To test whatever is up:

    mvn test

To target a specific vagrant instance

    mvn test -Dhost=ubuntu1404

For a list of possible systems

    vagrant status

To bring up an instance

    vagrant up ubuntu1404

Use blaze to run various tasks

    java -jar blaze.jar