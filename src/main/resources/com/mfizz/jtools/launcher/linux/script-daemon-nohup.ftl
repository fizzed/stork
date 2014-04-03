
#
# usage for daemon using NOHUP method
#

usage()
{
    echo "Usage: $0 [-start|-stop|-run|-status}"
    exit 1
}

[[ ! -z $APP_ACTION_ARG ]] || usage


#
# can we write to log and run directories as target group/user?
#
if [ ! -d "$APP_RUN_DIR" ]; then
    mkdir -p "$APP_RUN_DIR"
    if [ ! -d "$APP_RUN_DIR" ]; then
        echo "Unable to create run dir: $APP_RUN_DIR"
        exit 1
    fi
fi
    
if [ ! -d "$APP_LOG_DIR" ]; then
    mkdir -p "$APP_LOG_DIR"
    if [ ! -d "$APP_LOG_DIR" ]; then
        echo "Unable to create log dir: $APP_LOG_DIR"
        exit 1
    fi
fi

# more intelligent info on location of nohup output
# if the initial working directory is not the same as the app home then
# use the full path to the outfile
NOHUP_OUT="$APP_LOG_DIR/$NAME.out"
if [ "$APP_HOME" != "$INITIAL_WORKING_DIR" ]; then
    NOHUP_OUT="$APP_HOME/$APP_LOG_DIR/$NAME.out"
fi





case "$APP_ACTION_ARG" in

  -start)
    echo -n "Starting $NAME: "
    verifyNotRunning $APP_PID_FILE

    # log start time first into outfile
    echo "$NAME starting at `date`" > "$NOHUP_OUT"

    nohup $RUN_CMD </dev/null >"$NOHUP_OUT" 2>&1 &
    PID=$!
    echo $PID > $APP_PID_FILE

    # TODO: can we confirm it actually started?

    echo "OK"

    echo "Please 'tail -f $NOHUP_OUT' for application output"

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
    echo $$ > $APP_PID_FILE
    eval $RUN_CMD
    # cleanup pid if it exists
    rm -f $APP_PID_FILE
    ;;

  -status)
    echo "Status for $NAME: "
    echo "app_home       =  $APP_HOME"
    echo "pid_file       =  $APP_PID_FILE"
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
