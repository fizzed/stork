
#
# usage for daemon using NOHUP method
#

usage()
{
    echo "Usage: $0 [--start|--start-run|--stop|--run|--status}"
    exit 1
}

[ ! -z "$APP_ACTION_ARG" ] || usage


#
# can we write to log directory (run directory checked previously)?
#    
if [ ! -d "$APP_LOG_DIR" ]; then
    mkdir -p "$APP_LOG_DIR" 2>/dev/null
    if [ ! -d "$APP_LOG_DIR" ]; then
        echo "Unable to create log dir: $APP_LOG_DIR_ABS (check permissions; is user `whoami` owner?)"
        exit 1
    fi
fi
if [ ! -w "$APP_LOG_DIR" ]; then
    echo "Unable to write files in log dir: $APP_LOG_DIR_ABS (check permissions; is user `whoami` owner?)"
    exit 1
fi


# more intelligent info on location of nohup output
# if the initial working directory is not the same as the app home then
# use the full path to the outfile
NOHUP_OUT="$APP_LOG_DIR_ABS/$NAME.out"
if [ "$APP_HOME" != "$INITIAL_WORKING_DIR" ]; then
    NOHUP_OUT="$APP_HOME/$APP_LOG_DIR/$NAME.out"
fi
# if outfile already exists, make sure it its writable by us
if [ -f "$NOHUP_OUT" ] && [ ! -w "$NOHUP_OUT" ]; then
    echo "Unable to overwrite existing nohup log file: $NOHUP_OUT (check permissions; is user `whoami` owner?)"
    exit 1
fi


case "$APP_ACTION_ARG" in

  --start)
    printf "Starting $NAME: "
    verifyNotRunning $APP_PID_FILE

    # log start time first into outfile
    echo "$NAME starting at `date`" > "$NOHUP_OUT"

    nohup "$JAVA_BIN" $RUN_ARGS </dev/null >"$NOHUP_OUT" 2>&1 &
    PID=$!
    echo $PID > $APP_PID_FILE

    # confirm the daemon started by making sure its alive for a certain time
    CONFIRMED=""
    if [ ! -z $DAEMON_MIN_LIFETIME ]; then
        # wait for minimum amount of time
        timeout=$DAEMON_MIN_LIFETIME
        while [ $timeout -gt 0 ]; do
            sleep 1
            # check if daemon not running
            if running "$APP_PID_FILE"; then
                printf "."
            else
                echo "failed"
                tail -n 100 "$NOHUP_OUT"
                exit 1
            fi
            timeout=`expr $timeout - 1`
        done
        CONFIRMED="min_lifetime"
    fi

    echo "OK"

    if [ -z $CONFIRMED ]; then
        echo "Please 'tail -f $NOHUP_OUT' for application output"
    fi
    ;;

  # best choice for running from systemd
  --start-run)
    printf "Starting $NAME: "
    verifyNotRunning $APP_PID_FILE

    # log start time first into outfile
    echo "$NAME starting at `date`" > "$NOHUP_OUT"

    nohup "$JAVA_BIN" $RUN_ARGS </dev/null >"$NOHUP_OUT" 2>&1 &
    PID=$!
    echo $PID > $APP_PID_FILE

    # confirm the daemon started by making sure its alive for a certain time
    CONFIRMED=""
    if [ ! -z $DAEMON_MIN_LIFETIME ]; then
        # wait for minimum amount of time
        timeout=$DAEMON_MIN_LIFETIME
        while [ $timeout -gt 0 ]; do
            sleep 1
            # check if daemon not running
            if running "$APP_PID_FILE"; then
                printf "."
            else
                echo "failed"
                tail -n 100 "$NOHUP_OUT"
                exit 1
            fi
            timeout=`expr $timeout - 1`
        done
        CONFIRMED="min_lifetime"
    fi

    echo "OK"

    if [ -z $CONFIRMED ]; then
        echo "Please 'tail -f $NOHUP_OUT' for application output"
    fi
    ;;

  --stop)
    printf "Stopping $NAME: "
    if running "$APP_PID_FILE"; then
      stopJavaApp "$APP_PID_FILE"
      echo "OK"
    else
      echo "not running"
    fi
    ;;

  --run)
    #echo "Running $NAME: "
    # some launcher frameworks manage the PID (this skips the check entirely)
    # only enable this env var if you know what you're doing
    if [ "$SKIP_PID_CHECK" = "0" ]; then
        verifyNotRunning $APP_PID_FILE
    fi
    # take pid of shell for pid lock
    echo $$ > $APP_PID_FILE
    # best effort to remove pid file upon exit via trap
    trap 'echo "cleaning up pid file: $APP_PID_FILE"; rm -f "$APP_PID_FILE"' 2 3 6 15
    eval $RUN_CMD
    ;;

  --status)
    echo "Status for $NAME: "
    echo "app_home: $APP_HOME"
    echo "run_dir: $APP_RUN_DIR_DEBUG"
    echo "log_dir: $APP_LOG_DIR_DEBUG"
    echo "lib_dir: $APP_LIB_DIR_DEBUG"
    echo "pid_file: $APP_PID_FILE_DEBUG"
    echo "java_min_version_required: $MIN_JAVA_VERSION"
    echo "java_bin: $JAVA_BIN"
    echo "java_version: $JAVA_VERSION"
    echo "java_run: $RUN_CMD"
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
