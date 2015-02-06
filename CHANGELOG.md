
#### To Be Released
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
 - Added new feature: stork-maven-assembly (creates tarball from target/stage)
 - Added new feature: stork-play-assembly (creates launcher for play app and
     also creates tarball for distribution)
 - Added new feature: stork-fabric-deploy (rapid deployment to remote system)

#### 1.1.0 - 2014-10-29
 - Renamed project to Stork.
 - Improved docs
 - Support for directories and wildcards with input file arguments

#### 1.0.1 - 2014-10-29
 - Reorganized project layout in sub-modules.

#### 1.0.0 - 2014-03-18
 - Initial release
