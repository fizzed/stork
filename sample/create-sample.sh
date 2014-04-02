#!/bin/sh

mvn clean

# all libs to target/dependency/ 
mvn package

rm -Rf lib
mkdir -p lib
cp target/dependency/* lib/

mvn jar:jar
cp target/*.jar lib/
rm -Rf lib/*-sources.jar
