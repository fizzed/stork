
if "%LAUNCHER_DEBUG%"=="1" (
    echo ^[LAUNCHER^] java_bin: %java_bin_accepted%
)

"%java_bin_accepted%" -cp "%APP_LIB_DIR%\*" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%
