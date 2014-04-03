
source $(dirname $0)/script-functions.sh

JAVA_CMD=`findJavaCommand`
echo "java_cmd: $JAVA_CMD"

JAVA_HOME=`findJavaHome`
echo "java_home: $JAVA_HOME"

java_cmds=`findJavaCommands`
echo $java_cmds

java_bin=`findMinJavaVersion "1.7" "$java_cmds"`
echo $java_bin

# split by \n char into array
#IFS=":" read -a array <<< "$java_cmds"
#for element in "${array[@]}"; do
#    echo "$element"
#done

#IFS=""
#java_bin=`findMinJavaVersion "1.7" '$java_cmds'`
#echo $java_bin

#IFS=""
#for java_cmd in $java_cmds; do
#    echo -e "java: $java_cmd"
#done
