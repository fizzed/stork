
#
# find java runtime that meets our minimum requirements
#
ALL_JAVA_BINS=`findAllJavaExecutables`

JAVA_BIN=`findFirstJavaExecutableByMinimumMajorVersion "$ALL_JAVA_BINS" "$MIN_JAVA_VERSION"`

if [ -z "$JAVA_BIN" ]; then
    echo "Unable to find Java runtime on system with version >= $MIN_JAVA_VERSION"

    min_java_maj_ver=`parseJavaMajorVersion "$MIN_JAVA_VERSION"`

    if [ -f "/etc/debian_version" ]; then
        echo "Try running 'sudo apt-get install openjdk-$min_java_maj_ver-jre-headless' or"
    elif [ -f "/etc/redhat-release" ]; then
        echo "Try running 'su -c \"yum install java-1.$min_java_maj_ver.0-openjdk\"' OR"
    fi

    echo "Visit http://java.com to download and install one for your system"
    exit 1
fi

JAVA_VERSION=`getJavaVersion "$JAVA_BIN"`


#
# build classpath either in absolute or relative form
#
if [ $WORKING_DIR_MODE = "RETAIN" ]; then
    # absolute to app home
    APP_JAVA_CLASSPATH=`buildJavaClasspath "$APP_HOME/$LIB_DIR"`
    APP_LIB_DIR_DEBUG="$APP_HOME/$LIB_DIR"
else
    # jars will be relative to working dir (app home)
    APP_JAVA_CLASSPATH=`buildJavaClasspath "$LIB_DIR"`
    APP_LIB_DIR_DEBUG="<app_home>/$LIB_DIR"
fi


#
# classpath have anything?
#
if [ -z "$APP_JAVA_CLASSPATH" ]; then
    echo "No jars found for loading into classpath (empty lib dir? $APP_HOME/$LIB_DIR)"
    exit 1
fi


#
# special case for daemon: first argument to script should be action
#
APP_ACTION_ARG=

# first arg for a daemon is the action to do such as start vs. stop
if [ "$TYPE" = "DAEMON" ] && [ $# -gt 0 ]; then
  APP_ACTION_ARG=$1
  shift
  # append system property
  JAVA_ARGS="$JAVA_ARGS -Dlauncher.action=$APP_ACTION_ARG"
fi

# append extra app and java args
JAVA_ARGS="$JAVA_ARGS $EXTRA_JAVA_ARGS"
APP_ARGS="$APP_ARGS $EXTRA_APP_ARGS"

for a in "$@"; do
    if [ $LAUNCHER_DEBUG = "1" ]; then echo "[LAUNCHER] processing arg: $a"; fi

    # does the argument need escaped?
    if [ "$a" = `echo "$a" | sed 's/ //g'` ]; then
        # java system property and processing enabled?
        case "$a" in
            -D*)
                JAVA_ARGS="$JAVA_ARGS $a" ;;
            *)
                APP_ARGS="$APP_ARGS $a" ;;
        esac
    else
        # java system property and processing enabled?
        case "$a" in
            -D*)
                JAVA_ARGS="$JAVA_ARGS \"$a\"" ;;
            *)
                APP_ARGS="$APP_ARGS \"$a\"" ;;
        esac
    fi

    shift
done


SYS_MEM_MB=`getSystemMemoryMB`
logLauncherDebug "detected system memory: $SYS_MEM_MB MB"


#
# add max memory java option (if specified)
#
if [ ! -z $JAVA_MAX_MEM_PCT ]; then
    if [ $SYS_MEM_MB -le 0 ]; then
        echo "Unable to detect system memory to set java max memory"
        exit 1
    fi
    MM=`pctOf $SYS_MEM_MB $JAVA_MAX_MEM_PCT`
    JAVA_ARGS="-Xmx${r"${MM}"}m $JAVA_ARGS"
elif [ ! -z $JAVA_MAX_MEM ]; then
    JAVA_ARGS="-Xmx${r"${JAVA_MAX_MEM}"}m $JAVA_ARGS"
fi


#
# add min memory java option (if specified)
#
if [ ! -z $JAVA_MIN_MEM_PCT ]; then
    if [ $SYS_MEM_MB -le 0 ]; then
        echo "Unable to detect system memory to set java min memory"
        exit 1
    fi
    MM=`pctOf $SYS_MEM_MB $JAVA_MIN_MEM_PCT`
    JAVA_ARGS="-Xms${r"${MM}"}m $JAVA_ARGS"
elif [ ! -z $JAVA_MIN_MEM ]; then
    JAVA_ARGS="-Xms${r"${JAVA_MIN_MEM}"}m $JAVA_ARGS"
fi


#
# if a daemon is being run in foreground then the type is still console
#
RUN_TYPE=$TYPE
if [ "$APP_ACTION_ARG" = "-run" ]; then
    RUN_TYPE="CONSOLE"
fi


#
# symlink of java requested?
# this may break on some systems so we need to test it works
#
if [ "$SYMLINK_JAVA" = "1" ]; then
    TARGET_SYMLINK="$APP_RUN_DIR/$NAME-java"
    # if link already exists then try to delete it
    if [ -L "$TARGET_SYMLINK" ]; then
        rm -f "$TARGET_SYMLINK"
    fi
    ln -s "$JAVA_BIN" "$TARGET_SYMLINK" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        # symlink succeeded (test if it works)
        symlink_test=$("$TARGET_SYMLINK" -version 2>/dev/null)
        if [ $? -eq 0 ]; then
            # symlink worked
            NON_SYMLINK_JAVA_BIN="$JAVA_BIN"
            JAVA_BIN="$TARGET_SYMLINK"
        else
            if [ $LAUNCHER_DEBUG = "1" ]; then echo "[LAUNCHER] symlink failed for java; ignoring"; fi
        fi
    fi
fi


#
# create java command to execute
#

RUN_ARGS="-Dlauncher.name=$NAME -Dlauncher.type=$RUN_TYPE -Dlauncher.app.dir='$APP_HOME' -classpath \"$APP_JAVA_CLASSPATH\" $JAVA_ARGS $MAIN_CLASS $APP_ARGS"
RUN_CMD="\"$JAVA_BIN\" $RUN_ARGS"

#
# debug for either console/daemon apps
#
logLauncherDebug "working_dir: `pwd`"
logLauncherDebug "app_home: $APP_HOME"
logLauncherDebug "run_dir: $APP_RUN_DIR_DEBUG"
logLauncherDebug "log_dir: $APP_LOG_DIR_DEBUG"
logLauncherDebug "lib_dir: $APP_LIB_DIR_DEBUG"
logLauncherDebug "pid_file: $APP_PID_FILE_DEBUG"
logLauncherDebug "java_min_version_required: $MIN_JAVA_VERSION"
if [ ! -z "$NON_SYMLINK_JAVA_BIN" ]; then
    logLauncherDebug "java_bin: $NON_SYMLINK_JAVA_BIN"
    logLauncherDebug "java_symlink: $JAVA_BIN"
else
    logLauncherDebug "java_bin: $JAVA_BIN"
fi
logLauncherDebug "java_version: $JAVA_VERSION"
logLauncherDebug "java_run: $RUN_CMD"