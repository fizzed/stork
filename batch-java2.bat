
@echo off
setlocal ENABLEEXTENSIONS




set KEY_NAME="HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"
set VALUE_NAME=CurrentVersion
::
:: get the current version
::
FOR /F "usebackq skip=2 tokens=3" %%A IN (`REG QUERY %KEY_NAME% /v %VALUE_NAME% 2^>nul`) DO (
    set ValueValue=%%A
)
if defined ValueValue (
    @echo the current Java runtime is  %ValueValue%
) else (
    @echo %KEY_NAME%\%VALUE_NAME% not found.
    goto end
)
set JAVA_CURRENT="HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\%ValueValue%"
set JAVA_HOME=JavaHome
::
:: get the javahome
::
FOR /F "usebackq skip=2 tokens=3*" %%A IN (`REG QUERY %JAVA_CURRENT% /v %JAVA_HOME% 2^>nul`) DO (
    set JAVA_PATH=%%A %%B
)
echo the path of the current Java JVM according to the registry is 
echo %JAVA_PATH%
echo.
echo now if we try it :
"%JAVA_PATH%\bin\java.exe" -version
:end