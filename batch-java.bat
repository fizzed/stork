@echo off

@REM https://gist.github.com/djangofan/1445440

@REM first java_home env
IF DEFINED JAVA_HOME (
  ECHO JAVA_HOME is already set to !JAVA_HOME!
  CALL :STRIP "!JAVA_HOME!" JHOME
  ECHO java_home: !JHOME!
  
  call :IsJavaBin "!JHOME!\bin\java"
  
  call :IsJavaBin "!JHOME!\jre\bin\java2"
)

@REM query registry
reg query "HKLM\Software\JavaSoft\Java Runtime Environment"

reg query "HKLM\Software\JavaSoft\Java Development Kit"


@echo off
"%JAVA_HOME%"\bin\java -version:1.8 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto found
echo NOT FOUND
goto :end

:found
echo FOUND

:IsJavaBin
setlocal
SET java_bin=%~1
echo checking if %java_bin% is valid
"%java_bin%" -version > nul 2>&1
if %ERRORLEVEL% == 0 (
	echo  valid 
) else (
	echo not valid
)
exit /b %ERRORLEVEL%
GOTO:EOF

:STRIP
setlocal
REM Strip quotes and extra backslash from string
SET n=%~1
SET n=%n:\\=\%
SET n=%n:"=%
( endlocal
	set "%2=%n%"
)
GOTO:EOF

:END