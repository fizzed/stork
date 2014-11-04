clean:
	mvn clean
	cd examples/hello-server-dropwizard; mvn clean

package:
	mvn package

hello-console: package
	examples/hello-world/target/stage/bin/hello-console

hello-daemon: package
	examples/hello-world/target/stage/bin/hello-daemon -run

hello-server-dropwizard: package
	cd examples/hello-server-dropwizard; stork-maven-assembly
	examples/hello-server-dropwizard/target/stage/bin/hello-server-dropwizard -run

hello-server-play: package
	cd examples/hello-server-play; stork-play-assembly
	examples/hello-server-play/target/stage/bin/hello-server-play -run
	
