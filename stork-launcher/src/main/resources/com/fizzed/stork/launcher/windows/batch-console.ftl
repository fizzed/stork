
@REM
@REM Process arguments into either app or java system property
@REM With recursion used the various labels help prevent multiple executions
@REM 
call :ProcessArgs %*
goto :ProcessedArgs

:ProcessArgs
for /F "tokens=1*" %%a in ("%*") do (
  set arg=%%a
  IF "!arg:~0,2!"=="-D" (
    echo java arg: !arg!
    set JAVA_ARGS=%JAVA_ARGS% !arg!
  ) else (
    echo app arg: !arg!
    set APP_ARGS=%APP_ARGS% !arg!
  )
  if NOT x%%b==x call :ProcessArgs %%b
)
goto :eof
:ProcessedArgs


if "%LAUNCHER_DEBUG%"=="1" (
@ECHO ON
)

"%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%

@ECHO OFF

if %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
