
Sample "hello world" console and daemon apps.

This project just needs to be compiled once and then will be reused by the
parent project to create sample console and daemon projects.

The NanoHTTPD library this sample uses unfortunatley does not publish
artifacts to Maven central -- so you'll need to clone the repo and install
it locally:

git clone https://github.com/NanoHttpd/nanohttpd.git
mvn install
