Stork by Fizzed
=======================================

### Contributors

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

### Setup

#### On Linux/OSX

	export PATH=`pwd`/assembly/target/stage/bin:$PATH

#### On Windows

	set PATH=%CD%\assembly\target\stage\bin;%PATH%

### Testing

	ant hello-console
	ant hello-daemon
	ant hello-server-dropwizard
	ant hello-server-play


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
