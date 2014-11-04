
if "%LAUNCHER_DEBUG%"=="1" (
@ECHO ON
)

"%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS% %*

@ECHO OFF