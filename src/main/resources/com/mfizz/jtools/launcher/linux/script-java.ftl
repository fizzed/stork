
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

JAVA_VERSION=`getJavaVersion "$JAVA_BIN"`

#
# build classpath either in absolute or relative form
#
if [ $WORKING_DIR_MODE == "RETAIN" ]; then
  # absolute to app home
  APP_JAVA_CLASSPATH=`buildJavaClasspath $APP_HOME/$JAR_DIR`
  JAR_DIR_DEBUG="$APP_HOME/$JAR_DIR"
else
  # jars will be relative to working dir (app home)
  APP_JAVA_CLASSPATH=`buildJavaClasspath $JAR_DIR`
  JAR_DIR_DEBUG="<app_home>/$JAR_DIR"
fi

#
# special case for daemon: first argument to script should be action
#
APP_ACTION_ARG=

# first arg for a daemon is the action to do such as start vs. stop
if [ "$TYPE" == "DAEMON" ] && [ $# -gt 0 ]; then
  APP_ACTION_ARG=$1
  shift
  # append system property
  JAVA_ARGS="$JAVA_ARGS -Dlauncher.action=$APP_ACTION_ARG"
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
#

# if a daemon is being run in foreground then the type is still console
RUN_TYPE=$TYPE
if [ "$APP_ACTION_ARG" == "-run" ]; then
    RUN_TYPE="CONSOLE"
fi

RUN_ARGS="-Dlauncher.name=$NAME -Dlauncher.type=$RUN_TYPE -cp $APP_JAVA_CLASSPATH $JAVA_ARGS $MAIN_CLASS $APP_ARGS"
RUN_CMD="$JAVA_BIN $RUN_ARGS"

#
# debug for either console/daemon apps
#
if [[ $DEBUG ]]; then
    echo "[launcher] working_dir: `pwd`"
    echo "[launcher] app_home: $APP_HOME"
    echo "[launcher] run_dir: $APP_RUN_DIR_DEBUG"
    echo "[launcher] log_dir: $APP_LOG_DIR_DEBUG"
    echo "[launcher] jar_dir: $JAR_DIR_DEBUG"
    echo "[launcher] pid_file: $APP_PID_FILE_DEBUG"
    echo "[launcher] java_min_version_required: $MIN_JAVA_VERSION"
    echo "[launcher] java_bin: $JAVA_BIN"
    echo "[launcher] java_version: $JAVA_VERSION"
    echo "[launcher] java_run: $RUN_CMD"
fi