
set APP_CLASSPATH="%APP_LIB_DIR%\*"

if "%LAUNCHER_DEBUG%"=="1" (
    echo ^[LAUNCHER^] java_bin: %java_bin_accepted%
)

"%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%
