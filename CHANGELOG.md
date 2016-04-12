Stork by Fizzed
=======================================

#### 2.x.x - xxxx-xx-xx

 - Refactored and polished project layout
 - Runs on Java 1.7+
 - stork-sbt-play-plugin is deprecated (Fizzed has moved away from scala w/
   Java8 and we didn't have time to update this dependency, PRs welcome)
 - All artifact ids now simply are "stork-" rather than "fizzed-stork-"
 - Fixed bug with windows .bat launchers not returning an error code if a JVM
   was not found
 - Fixed bug with java homes on linux/bsd that contained spaces not running
 - Vagrant used to unit test and verify launchers work across numerous 
   operating system distributions.

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
