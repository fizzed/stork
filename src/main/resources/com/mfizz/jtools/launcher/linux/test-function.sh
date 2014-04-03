
source $(dirname $0)/script-functions.sh

JAVA_CMD=`findJavaCommand`
echo "java_cmd: $JAVA_CMD"

JAVA_HOME=`findJavaHome`
echo "java_home: $JAVA_HOME"

java_cmds=`findJavaCommands`
for java_cmd in $java_cmds; do
    echo "java: $java_cmd"
done