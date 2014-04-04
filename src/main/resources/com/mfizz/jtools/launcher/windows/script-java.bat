
:: https://gist.github.com/djangofan/1445440

:: search environment section
IF DEFINED JAVA_HOME (
  ECHO JAVA_HOME is already set to !JAVA_HOME!
  CALL :STRIP "!JAVA_HOME!">"!SCRIPTDIR!\javahome.txt"
  ECHO Created !SCRIPTDIR!\javahome.txt file containing JAVA_HOME
  GOTO :END
)

:STRIP
REM Strip quotes and extra backslash from string
SET n=%~1
SET n=%n:\\=\%
SET n=%n:"=%
IF NOT "%n%"=="" ECHO %n%
GOTO :EOF

reg query "HKLM\Software\JavaSoft\Java Runtime Environment"


@echo off
"%JAVA_HOME%"\bin\java -version:1.8 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto found
echo NOT FOUND
goto end

:found
echo FOUND
:end