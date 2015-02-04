Stork by Fizzed
=======================================

## Contributors

 - [Fizzed, Inc.](http://fizzed.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

## Compiling

    mvn install

## Release
    
The "release" profile will only publish a subset of projects to Maven (e.g.
it won't publish the example projects to Maven Central). Also, the release plugin
does not "install" to local repo during prepare which breaks the SBT build
of this project. To workaround this error:

mvn release:prepare -Prelease
mvn install
mvn release:prepare -Prelease -Dresume=true

Then release sbt-play-plugin... need to hardcode version we are releasing

cd sbt-play-plugin
activator +publishSigned

## Testing command-line version during development.

### On Linux/OSX

    export PATH=`pwd`/cli/target/stork/bin:$PATH

### On Windows

    set PATH=%CD%\cli\target\stork\bin;%PATH%

## Testing

    ant hello-console
    ant hello-daemon
    ant hello-server-dropwizard
    ant hello-server-play


## Notes for target operating systems

### Windows

Daemonizers considered:

	http://jslwin.sourceforge.net/
	https://github.com/kohsuke/winsw

### Mac OSX

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

### Resources

Jenkins project:
	https://github.com/sbt/sbt-native-packager/tree/master/src/main/resources/com/typesafe/sbt/packager/archetypes

	https://gist.github.com/djangofan/1445440
