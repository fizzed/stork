Sample of using Mfizz java-tools to create launcher scripts for java apps.

To create single launcher script:
 ../bin/java-generate-launcher -c src/main/launcher/hello-world.conf -b bin

To create multiple launcher scripts:
 for f in $(ls src/main/launcher/*); do ../bin/java-generate-launcher -c $f; done
