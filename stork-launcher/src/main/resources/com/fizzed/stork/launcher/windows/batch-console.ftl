
if "%LAUNCHER_DEBUG%"=="1" (
@ECHO ON
)

"%java_bin_accepted%" -Dlauncher.name=%NAME% -Dlauncher.type=%RUN_TYPE% "-Dlauncher.app.dir=%APP_HOME%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%

@ECHO OFF

if %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
