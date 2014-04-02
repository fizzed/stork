
if [[ $SINGLE_INSTANCE -eq 1 ]]; then
  if running $APP_PID_FILE; then
    echo "$NAME already running with pid=`cat $APP_PID_FILE` (only single instance permitted)"
    exit 1
  else
    # take pid of shell for pid lock of single instance
    echo $$ > $APP_PID_FILE
  fi
fi

# console app ready to run!
$RUN_CMD

# cleanup pid if it exists
rm -f $APP_PID_FILE

