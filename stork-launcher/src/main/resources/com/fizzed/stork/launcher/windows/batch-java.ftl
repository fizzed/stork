
@REM Bug with Java7 <= build 8 on wildcard classpath expansion
@REM easy solution is to append a semi-colon at end onto classpath
@REM http://stackoverflow.com/questions/9195073/broken-wildcard-expansion-for-java7-commandline-on-windows7
set APP_CLASSPATH=%APP_LIB_DIR%\*;

if "%LAUNCHER_DEBUG%"=="1" (
    echo ^[LAUNCHER^] java_classpath: %APP_CLASSPATH%
    echo ^[LAUNCHER^] java_bin: %java_bin_accepted%
)

@REM append extra app and java args?

if NOT "%EXTRA_JAVA_ARGS%"=="" (
  set JAVA_ARGS=%JAVA_ARGS% %EXTRA_JAVA_ARGS%
)

if NOT "%EXTRA_APP_ARGS%"=="" (
  set APP_ARGS=%APP_ARGS% %EXTRA_APP_ARGS%
)

@REM
@REM Process arguments into either app or java system property
@REM With recursion used the various labels help prevent multiple executions
@REM 
call :ProcessArgs %*
goto :ProcessedArgs

:ProcessArgs
for /F "tokens=1*" %%a in ("%*") do (
  set arg=%%a

  @REM quoted?
  IF !arg:~0^,1!!arg:~-1! equ "" (
    IF "!arg:~1,3!"=="-D" (
      set JAVA_ARGS=%JAVA_ARGS% !arg!
    ) ELSE (
      set APP_ARGS=%APP_ARGS% !arg!
    )
  ) ELSE (
    IF "!arg:~0,2!"=="-D" (
      set JAVA_ARGS=%JAVA_ARGS% !arg!
    ) ELSE (
      set APP_ARGS=%APP_ARGS% !arg!
    )
  )
  
  if NOT x%%b==x call :ProcessArgs %%b
)
goto :eof
:ProcessedArgs

@REM
@REM prepend -Xrs flag?
@REM
if "%INCLUDE_JAVA_XRS%"=="1" (
  set JAVA_ARGS=-Xrs %JAVA_ARGS%
)
