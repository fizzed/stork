Stork by Fizzed
=======================================

#### 2.5.0 - 2017-09-05

 - stork-launcher: Command-line arguments starting with "-D" are now detected and passed to
   your application as Java System Properties rather than as application
   arguments. (@jjlauer)
 - stork-launcher: Daemons across all platforms now passthrough application arguments or
   System properties for the "--run" command.
 - stork-launcher: New "EXTRA_APP_ARGS" and "EXTRA_JAVA_ARGS" configuration/variables
   so that you can define standard ones for your app, but allow users to pass in
   extra variables vs. only being able to override what you provided.
 - stork-launcher: Cygwin compatability on Windows. The linux scripts all work on Windows when
   running in a cygwin environment. (@jjlauer)
 - stork-launcher: Detect APP_HOME first before initializing other variables. Allows using it
   to help build other properties. (@BertrandA)
 - stork-deploy: Added `/etc/init.d/crond` while trying to detect for SYSV init (@erbrecht)
 - Dependencies all updated to latest versions.

#### 2.4.0 - 2016-12-09

 - Add new `unattended` option to cli and deploy options to prevent prompts during deploy

#### 2.3.0 - 2016-09-07

 - `stork-deploy` has all blaze-* dependencies in `provided` scope to help
   avoid version conflicts when directly used in [Blaze](https://github.com/fizzed/blaze)

#### 2.2.1 - 2016-08-31

 - Fixed issue with systemd services not starting at boot. Removed `alias` from
   default systemd template

#### 2.2.0 - 2016-08-29

 - Support for `systemd`! `stork-launcher` will generate `share/systemd` service
   files and `stork-deploy` will intelligently deploy it.
 - Support for deploying thru SSH proxy/bastion/jump hosts.  If your `.ssh/config`
   file uses any common `ProxyCommand` options then stork will likely work out-of-the-box.
   Please see [blaze ssh docs](https://github.com/fizzed/blaze/blob/master/docs/SSH.md)
   for more info.
 - Launchers for windows now preserve environment variables allowing users to
   override properties on an execution.
 - Fixed bug where temp deploy files were not always deleted (especially on windows)
 - Added Ubuntu 16.04 as reference system to verify launchers & deployments.
 - Dependencies updated:
    - blaze v0.11.1 to v0.12.0
    - jackson v2.7.3 to v2.8.1
    - slf4j v1.7.20 to v1.7.21
    - commons-compress v1.9 to v1.12
    - crux-vagrant v0.3.2 to v0.4.0

#### 2.1.1 - 2016-06-24

 - Fix for environment variables (such as JAVA_ARGS) that contained spaces
   causing a launcher script to partially fail. 

#### 2.1.0 - 2016-06-23

 - Deploys are assigned a UUID
 - Fixed issue w/ multiple users deploying to same target being unable to 
   delete each others temp work dir.  The work dir now has the deploy UUID
   appended so each user + deploy gets their own unique deploy work dir.

#### 2.0.1 - 2016-05-19

 - Allow specifying APP_ARGS and JAVA_ARGS in /etc/default/myapp (gitblit)

#### 2.0.0 - 2016-04-13

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
