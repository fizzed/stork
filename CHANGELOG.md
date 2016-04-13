Stork by Fizzed
=======================================

#### 2.x.x - xxxx-xx-xx

Major

 - Major refactor and much more polished project layout
 - Requires Java 1.7+
 - All project artifacts now starts with "stork-" rather than "fizzed-stork-"
 - stork-sbt-play-plugin deprecated (Fizzed has moved away from scala w/
   Java8 and we didn't have time to update this dependency, PRs welcome)
 - Vagrant used to unit test and verify launchers work across numerous 
   operating system distributions.
 - Launchers certified on Windows, Mac, Ubuntu 14.04, CentOS 7, Debian 8,
   FreeBSD 10.2, and OpenBSD 5.8.  You can also easily verify another platform
   by suppling a vagrant instance and running "mvn test -Dhost=name" with the target
 - Command-line stork-generate renamed to stork-launcher with more polished
   interface and arguments.
 - stork-fabric-deploy deprecated and replaced with Java-based stork-deploy
 - stork-deploy is embeddable within other Java apps
 - stork-deploy supports everything stork-fabric-deploy did, but with numerous
   new features.
    - Secure deployments by using strict permissions
    - Deploy config files supported (in simple java properties format)
    - Configurable prefix directory, user, and group
    - Initial systemd support

Minor

 - Fixed bug with windows .bat launchers not returning an error code if a JVM
   was not found
 - Fixed bug with unix launchers not handling java dirs with spaces in the 
   dir name
 - Bump to JslWin v0.99l

#### 1.2.3 - 2015-03-13

 - stork-fabric-deploy: use_ssh_config true by default

#### 1.2.2 - 2015-02-03

 - Migrated packages from co.fizzed to com.fizzed

#### 1.2.1 - 2014-11-20

 - Fixed bug with template for launchers on linux when java memory setting used
   a value >= 999 (freemarker was adding commas).
 - Java detect helper script now excluded by default. Can be included with a new
   configuration setting "include_java_detect_helper" set to true.
 - Fixed bug with stork-fabric-deploy for console-only apps

#### 1.2.0 - 2014-11-04

 - Added support for stork-maven-assembly (creates tarball from target/stage)
 - Added support for stork-play-assembly (creates launcher for play app and
     also creates tarball for distribution)
 - Added support for stork-fabric-deploy (rapid deployment to remote system)

#### 1.1.0 - 2014-10-29

 - Renamed project to Stork.
 - Improved docs
 - Support for directories and wildcards with input file arguments

#### 1.0.1 - 2014-10-29

 - Reorganized project layout in sub-modules.

#### 1.0.0 - 2014-03-18

 - Initial release
