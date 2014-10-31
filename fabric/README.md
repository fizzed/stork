# requires python and pip installed

sudo pip install fabric

cd ../examples/hello-server-dropwizard && make assembly

fab -H sea-web2.fizzed.lan deploy:assembly=../examples/hello-server-dropwizard/target/hello-server-dropwizard-1.0.0-SNAPSHOT.tar.gz
