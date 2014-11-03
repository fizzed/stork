Stork by Fizzed
=======================================

### Contributors

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Development

Since this app creates launchers for other Java apps, a somewhat unusual build system
was required for testing & assembly during development and distribution.  There is
an Ant-based build.xml script which in turn creates maven commands for compiling, etc.
So you'll need both ant and maven available if you plan on building from source. The
assembled distribution, however, uses "console" launcher scripts generated from this
project -- so this application can easily run on Windows, Linux, Mac OSX, etc.

### On Linux/OSX

	export PATH=`pwd`/assembly/target/stage/bin:$PATH

### On Windows



### Notes for target operating systems

#### Windows

Daemonizers considered:

	http://jslwin.sourceforge.net/
	https://github.com/kohsuke/winsw

#### Mac OSX

References:
    http://couchdb.readthedocs.org/en/latest/install/mac.html
    http://vincent.bernat.im/en/blog/2013-autoconf-osx-packaging.html
    https://github.com/jenkinsci/jenkins/tree/master/osx

Standard install location for daemons:
    /Library/Application Support/<app_display_name>

    sudo mkdir -p /Library/Application\ Support/Hello\ Server
    sudo cp -R target/sample/* /Library/Application\ Support/Hello\ Server/

Standard install location for launchd startup script:
    sudo cp target/sample/share/osx/com.example.hello-daemon.plist /Library/LaunchDaemons/

/Library/LaunchDaemons System-wide daemons provided by the administrator.
started and available at boot (even if no user logged in yet).
    
To load (and start) daemon. Required in order to trigger on next boot.
    sudo launchctl load -F /Library/LaunchDaemons/com.example.hello-daemon.plist

To see what is going on (should have a PID value):
    sudo launchctl list com.example.hello-daemon

To unload (stop) daemon:
    sudo launchctl unload /Library/LaunchDaemons/com.example.hello-daemon.plist

#### Resources

Jenkins project:
	https://github.com/sbt/sbt-native-packager/tree/master/src/main/resources/com/typesafe/sbt/packager/archetypes

	https://gist.github.com/djangofan/1445440
