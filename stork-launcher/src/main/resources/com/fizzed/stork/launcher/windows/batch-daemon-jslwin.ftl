
@REM
@REM usage with no parameters?
@REM

IF %1.==. GOTO PrintUsageJSLWin

@REM
@REM log directory MUST MUST MUST exist or the service will fail to start
@REM

if not exist "%APP_LOG_DIR%" (
    mkdir "%APP_LOG_DIR%"
)

@REM
@REM execute argument command
@REM

if %bit64%==n set TargetExe=${config.name}32.exe
if %bit64%==y set TargetExe=${config.name}64.exe

@REM
@REM pop first app argument off so we can see what the user wants to do
@REM
set ARG=
for /F "tokens=1*" %%a in ("%APP_ARGS%") do (
  set ARG=%%a
  set APP_ARGS=%%b
)

IF "%ARG%"=="--run" (
    "%java_bin_accepted%" -cp "%APP_CLASSPATH%" %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%
) ELSE IF "%ARG%"=="--startdebug" (
    "%APP_BIN_DIR%\%TargetExe%" -debug
) ELSE IF "%ARG%"=="--start" (
    net start "${config.displayName}"
) ELSE IF "%ARG%"=="--stop" (
    net stop "${config.displayName}"
) ELSE IF "%ARG%"=="--install" (
    "%APP_BIN_DIR%\%TargetExe%" -install
) ELSE IF "%ARG%"=="--uninstall" (
    "%APP_BIN_DIR%\%TargetExe%" -remove
) ELSE (
    echo Error unsupported argument
    GOTO PrintUsageJSLWin
)

GOTO endLabel

:PrintUsageJSLWin
REM annoying string escaping
echo Usage: ^[^-^-^r^u^n^|^-^-^s^t^a^r^t^|^-^-^s^t^o^p^|^-^-^ins^tall^|^-^-^un^inst^a^ll^|^-^-^s^t^a^r^t^d^eb^u^g]
goto :errorlabel
