
#
# usage for daemon
#
usage()
{
    echo "Usage: $0 [-start|-stop|-run|-status}"
    exit 1
}

[[ ! -z $APP_ACTION_ARG ]] || usage

#
# daemon package used
#
DAEMON=/usr/bin/daemon
DAEMON_ARGS="--name=$NAME --inherit" 


#
# exit if the package is not installed
#
if [ ! -x "$DAEMON" ]; then
    echo "Error: daemon package not installed" >&2
    exit 1
fi


case "$APP_ACTION_ARG" in

  -start)
    echo -n "Starting $NAME: "
    
    $DAEMON $DAEMON_ARGS -- $RUN_CMD

    #$DAEMON $DAEMON_ARGS --running && return 1

    #verifyNotRunning $APP_PID_FILE
    #nohup $RUN_CMD </dev/null 2>&1 | tee logfile.log &    
    #PID=$!
    #disown $PID
    #echo $PID > $APP_PID_FILE
    #echo "OK"
    ;;

  -stop)
    echo -n "Stopping $NAME: "
    if running "$APP_PID_FILE"; then
      stopJavaApp $APP_PID_FILE
      echo "OK"
    else
      echo "not running"
    fi
    ;;

  -run)
    #echo "Running $NAME in foreground: "
    
    echo $DAEMON $DAEMON_ARGS --chdir="$APP_HOME" --verbose --foreground -- $RUN_CMD

    #verifyNotRunning $APP_PID_FILE
    # take pid of shell for pid lock
    # shell script PID into file
    #echo $$ > $APP_PID_FILE
    #eval $RUN_CMD
    # cleanup pid if it exists
    #rm -f $APP_PID_FILE
    ;;

  -status)
    echo "Status for $NAME: "
    echo "pid_file       =  $APP_PID_FILE"
    echo "app_home       =  $APP_HOME"
    echo "java_bin       =  $JAVA_BIN"
    echo "java_version   =  $JAVA_VERSION"
    echo "java_run       =  $RUN_CMD"
    echo
    if running "$APP_PID_FILE"; then
      echo "$NAME running with pid="`cat $APP_PID_FILE`
      exit 0
    else
      echo "$NAME not running"
    fi
    exit 1
    ;;

  *)
    usage
	;;
esac

#
# end of script
#
