
#
# run console app
#

if [[ $DEBUG ]]; then
    echo "[launcher] app_home: $APP_HOME"
    echo "[launcher] jar_dir: $JAR_DIR"
    echo "[launcher] java_run: $RUN_CMD"
fi

eval $RUN_CMD

#
# end of script
#
