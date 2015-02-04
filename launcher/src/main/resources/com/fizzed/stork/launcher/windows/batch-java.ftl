
@REM Bug with Java7 <= build 8 on wildcard classpath expansion
@REM easy solution is to append a semi-colon at end onto classpath
@REM http://stackoverflow.com/questions/9195073/broken-wildcard-expansion-for-java7-commandline-on-windows7
set APP_CLASSPATH=%APP_LIB_DIR%\*;

if "%LAUNCHER_DEBUG%"=="1" (
    echo ^[LAUNCHER^] java_classpath: %APP_CLASSPATH%
    echo ^[LAUNCHER^] java_bin: %java_bin_accepted%
)
