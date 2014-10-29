
Sample "hello world" console and daemon apps.

### Using in your own project

This project is a special case where there is an Ant build script which runs
Maven commands underneath.  However, on your own system, once "fizzed-jtools-launcher"
is installed in your path, you can simply add "jtools-launcher-generate" in the maven
exec plugin during the package phase.

### Development

The NanoHTTPD library this sample uses unfortunatley does not publish
artifacts to Maven central -- so you'll need to clone the repo and install
it locally:

git clone https://github.com/NanoHttpd/nanohttpd.git
mvn install

### Running

ant hello-console
ant hello-daemon

### Distribution

ant assembly

Copy target/*.tar.gz to final resting place
