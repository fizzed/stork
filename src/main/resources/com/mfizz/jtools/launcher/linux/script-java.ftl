
<#--
APP_JAVA_CMD=`findJavaCommand`

if [ -z $APP_JAVA_CMD ]; then
  echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.5) in your PATH." 2>&2
  exit 1
fi

APP_JAVA_VERSION=`getJavaVersion $APP_JAVA_CMD`

if [ $PWD_TO_APP_HOME -eq 1 ]; then
  # relative to current dir (app home)
  APP_JAVA_CLASSPATH=`buildJavaClasspath $LIB_DIR`
else
  # absolute to app home
  APP_JAVA_CLASSPATH=`buildJavaClasspath $APP_HOME/$LIB_DIR`
fi

APP_JAVA_ARGS="$JAVA_ARGS -Dlauncher.app.name=$NAME -D${APP_DIR_PROPERTY_NAME}=$APP_HOME -D${WORKING_DIR_PROPERTY_NAME}=$INITIAL_WORKING_DIR"
APP_JAVA_OPTIONS=
SYS_MEM=
UNDER_MAVEN=0

# special case: are we running under maven and were the
# the dependencies created?
if [ -d "target" ] && [ -d "target/dependency" ]; then
  UNDER_MAVEN=1
  echo "Appears we are running under maven! (special classpath)..."
  MVN_CLASSPATH=`buildJavaClasspath target/dependency`
  APP_JARS=`buildJavaClasspath target`
  APP_JAVA_CLASSPATH=`appendJavaClasspath "$APP_JAVA_CLASSPATH" "$APP_JARS"`
  APP_JAVA_CLASSPATH=`appendJavaClasspath "$APP_JAVA_CLASSPATH" "$MVN_CLASSPATH"`
  echo "Using classpath: $APP_JAVA_CLASSPATH"
fi 

# strip out all initial -D arguments to pass to jvm
# first command could be "action" for daemon
# rest would be parameters to pass to app
APP_ACTION_ARG=

# first arg for a daemon is the action to do such as start vs. stop
if [ "$TYPE" == "daemon" ] && [ $# -gt 0 ]; then
  APP_ACTION_ARG=$1
  shift
  # append system property
  APP_JAVA_ARGS="$APP_JAVA_ARGS -Dlauncher.app.action=$APP_ACTION_ARG"
fi

# append any additional command line arguments
for a in "$@"; do
  if [[ $a == -D* ]]; then
    APP_JAVA_ARGS="$APP_JAVA_ARGS $a"
  else
    APP_ARGS="$APP_ARGS $a"
  fi
  shift
done

if [ "$CP_AS_ENV" == "1" ]; then
  export CLASSPATH=$APP_JAVA_CLASSPATH
else
  APP_JAVA_ARGS="$APP_JAVA_ARGS -classpath $APP_JAVA_CLASSPATH"
fi

# build min memory java option
if [ ! -z $JAVA_MIN_MEM_PCT ]; then
  if [ -z $SYS_MEM ]; then SYS_MEM=`systemMemory`; fi
  MM=`pctOf $SYS_MEM $JAVA_MIN_MEM_PCT`
  APP_JAVA_OPTIONS="$APP_JAVA_OPTIONS -Xmn${MM}M"
elif [ ! -z $JAVA_MIN_MEM ]; then
  APP_JAVA_OPTIONS="$APP_JAVA_OPTIONS -Xmn${JAVA_MIN_MEM}M"
fi

# build max memory java option
if [ ! -z $JAVA_MAX_MEM_PCT ]; then
  if [ -z $SYS_MEM ]; then SYS_MEM=`systemMemory`; fi
  MM=`pctOf $SYS_MEM $JAVA_MAX_MEM_PCT`
  APP_JAVA_OPTIONS="$APP_JAVA_OPTIONS -Xms${MM}M -Xmx${MM}M"
elif [ ! -z $JAVA_MAX_MEM ]; then
  APP_JAVA_OPTIONS="$APP_JAVA_OPTIONS -Xms${JAVA_MAX_MEM}M -Xmx${JAVA_MAX_MEM}M"
fi

# append jmx if enabled
if [ ! -z $JMX_PORT ]; then
  APP_JAVA_OPTIONS="$APP_JAVA_OPTIONS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
fi

RUN_CMD="$APP_JAVA_CMD $APP_JAVA_ARGS $APP_JAVA_OPTIONS $MAIN_CLASS $APP_ARGS"

# what would the pid file be?
APP_RUN_DIR="$APP_HOME/$RUN_DIR"
APP_PID_FILE="$APP_RUN_DIR/$NAME.pid"

# will a pid file be used?
if [ "$TYPE" == "daemon" ] || [[ $SINGLE_INSTANCE -eq 1 ]]; then
  if [ ! -d $APP_RUN_DIR ]; then 
    mkdir "$APP_RUN_DIR"
  fi
  if [ ! -w $APP_RUN_DIR ]; then
    echo "App run dir is not writable! [$APP_RUN_DIR]"
    exit 1
  fi
fi

-->

#
# find java runtime that meets our minimum requirements
#
ALL_JAVA_CMDS=`findJavaCommands`

JAVA_BIN=`findMinJavaVersion "$MIN_JAVA_VERSION" "$ALL_JAVA_CMDS"`

if [ -z "$JAVA_BIN" ]; then
    echo "Unable to find Java runtime on system with version >= $MIN_JAVA_VERSION"
    echo "Please visit http://java.com to download and install one for your system"
    exit 1
fi

# build classpath either in absolute or relative form
if [ $WORKING_DIR_MODE == "RETAIN" ]; then
  # absolute to app home
  APP_JAVA_CLASSPATH=`buildJavaClasspath $APP_HOME/$JAR_DIR`
else
  # jars will be relative to working dir (app home)
  APP_JAVA_CLASSPATH=`buildJavaClasspath $JAR_DIR`
fi

"$JAVA_BIN" -cp $APP_JAVA_CLASSPATH $JAVA_ARGS $MAIN_CLASS $APP_ARGS
