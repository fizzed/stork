
if "%LAUNCHER_DEBUG%"=="1" (
@ECHO ON
)

@REM https://blogs.oracle.com/quinn/a-little-trick-with-windows-bat-scripts-argument-processing-and-equals-signs
:recurse
for /F "tokens=1*" %%a in ("%*") do (
  IF "%%a:~0,2%"=="-D" (
    set JAVA_ARGS=%JAVA_ARGS% %%a
  ) else (
    set APP_ARGS=%APP_ARGS% %%a
  )
  if NOT x%%b==x (
    call :recurse %%b
  ) else (
    GOTO Continue
  )
)
:Continue


@REM https://stackoverflow.com/questions/7247195/parsing-of-cmdline-arguments-containing

@REM :Loop
@REM IF "%1"=="" GOTO Continue
@REM IF "%1:~0,2%"=="-D" (
@REM   set JAVA_ARGS=%JAVA_ARGS% %1
@REM ) else (
@REM   set APP_ARGS=%APP_ARGS% %1
@REM )
@REM SHIFT
@REM GOTO Loop
@REM :Continue

"%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%

@ECHO OFF

if %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
