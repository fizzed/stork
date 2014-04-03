
#
# usage for daemon
#

usage()
{
    echo "Usage: $0 {start|stop|run|status}"
    exit 1
}

[[ ! -z $APP_ACTION_ARG ]] || usage

case "$APP_ACTION_ARG" in

  -start)
    echo -n "Starting $NAME: "
    verifyNotRunning $APP_PID_FILE
    eval $RUN_CMD &
    PID=$!
    disown $PID
    echo $PID > $APP_PID_FILE
    echo "OK"
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
    echo "Running $NAME: "
    verifyNotRunning $APP_PID_FILE
    # take pid of shell for pid lock
    # shell script PID into file
    echo $$ > $APP_PID_FILE
    $RUN_CMD
    # cleanup pid if it exists
    rm -f $APP_PID_FILE
    ;;

  -status)
    echo "Status for $NAME: "
    echo "PID_FILE       =  $APP_PID_FILE"
    echo "APP_HOME       =  $APP_HOME"
    echo "JAVA_COMMAND   =  $APP_JAVA_CMD"
    echo "JAVA_VERSION   =  $APP_JAVA_VERSION"
    #echo "JAVA_ARGS      =  $APP_JAVA_ARGS"
    if [[ $CP_AS_ENV -eq 1 ]]; then
      echo "CLASSPATH      =  $APP_JAVA_CLASSPATH"
    fi
    echo "RUN_CMD        =  $RUN_CMD"
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
