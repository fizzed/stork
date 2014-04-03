stage-sample:
	mkdir -p target/sample
	cp -R sample/lib target/sample/
	
hello-console: stage-sample
	mvn -e compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.mfizz.jtools.launcher.Generator" -Dexec.args="-i src/test/resources/hello-console.yml -o target/sample/bin"
	target/sample/bin/hello-console
