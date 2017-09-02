
if "%LAUNCHER_DEBUG%"=="1" (
@ECHO ON
)

:Loop
IF "%1"=="" GOTO Continue
echo %1
@REM IF "%variable:~0,3%"=="ABC"
SHIFT
GOTO Loop
:Continue

"%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS% %*

@ECHO OFF

if %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
