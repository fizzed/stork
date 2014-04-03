
#
# find java runtime that meets our minimum requirements
#
ALL_JAVA_CMDS=`findJavaCommands`

JAVA_BIN=`findMinJavaVersion "$MIN_JAVA_VERSION" "$ALL_JAVA_CMDS"`

if [ -z "$JAVA_BIN" ]; then
    echo "Unable to find Java runtime on system with version >= $MIN_JAVA_VERSION"

    min_java_version=`extractPrimaryJavaVersion "$MIN_JAVA_VERSION"`

    if [ -f "/etc/debian_version" ]; then
        echo "Try running 'sudo apt-get install openjdk-$min_java_version-jre-headless' or"
    elif [ -f "/etc/redhat-release" ]; then
        echo "Try running 'su -c \"yum install java-1.$min_java_version.0-openjdk\"' OR"
    fi

    echo "Visit http://java.com to download and install one for your system"
    exit 1
fi

#
# build classpath either in absolute or relative form
#
if [ $WORKING_DIR_MODE == "RETAIN" ]; then
  # absolute to app home
  APP_JAVA_CLASSPATH=`buildJavaClasspath $APP_HOME/$JAR_DIR`
else
  # jars will be relative to working dir (app home)
  APP_JAVA_CLASSPATH=`buildJavaClasspath $JAR_DIR`
fi

#
# any additional command line arguments passed to this script?
# filter out -D arguments as they should go to java, not app
#
for a in "$@"; do

  if [[ $DEBUG ]]; then echo "[launcher] processing arg: $a"; fi

  if [[ $a == -D* ]]; then
    JAVA_ARGS="$JAVA_ARGS $a"
  else
    # does the argument need escaped?
    if [[ "$a" = "${"$"}{a% *}" ]]; then
        APP_ARGS="$APP_ARGS $a"
    else
        APP_ARGS="$APP_ARGS \"$a\""
    fi
  fi
  shift
done

#
# add max memory java option (if specified)
#
if [ ! -z $JAVA_MAX_MEM_PCT ]; then
  if [ -z $SYS_MEM ]; then SYS_MEM=`systemMemory`; fi
  if [ -z $SYS_MEM ]; then echo "Unable to detect system memory to set java max memory"; exit 1; fi
  MM=`pctOf $SYS_MEM $JAVA_MAX_MEM_PCT`
  JAVA_ARGS="-Xms${r"${MM}"}M -Xmx${r"${MM}"}M $JAVA_ARGS"
elif [ ! -z $JAVA_MAX_MEM ]; then
  JAVA_ARGS="-Xms${r"${JAVA_MAX_MEM}"}M -Xmx${r"${JAVA_MAX_MEM}"}M $JAVA_ARGS"
fi

#
# add min memory java option (if specified)
#
if [ ! -z $JAVA_MIN_MEM_PCT ]; then
  if [ -z $SYS_MEM ]; then SYS_MEM=`systemMemory`; fi
  if [ -z $SYS_MEM ]; then echo "Unable to detect system memory to set java max memory"; exit 1; fi
  MM=`pctOf $SYS_MEM $JAVA_MIN_MEM_PCT`
  JAVA_ARGS="-Xmn${r"${MM}"}M $JAVA_ARGS"
elif [ ! -z $JAVA_MIN_MEM ]; then
  JAVA_ARGS="-Xmn${r"${JAVA_MIN_MEM}"}M $JAVA_ARGS"
fi

#
# create java command to execute
# TODO:
#
RUN_CMD="\"$JAVA_BIN\" -Dlauncher.name=$NAME -Dlauncher.type=$TYPE -cp $APP_JAVA_CLASSPATH $JAVA_ARGS $MAIN_CLASS $APP_ARGS"
