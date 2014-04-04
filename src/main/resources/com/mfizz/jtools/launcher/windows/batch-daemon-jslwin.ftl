
@REM
@REM usage with no parameters?
@REM

IF %1.==. GOTO PrintUsageJSLWin

REM
REM log directory MUST exist or the service will fail to start
REM

if not exist "%APP_LOG_DIR" (
    mkdir "%APP_LOG_DIR"
)


REM
REM execute argument command
REM

if %bit64%==n set TargetExe=hello-daemon32.exe
if %bit64%==y set TargetExe=hello-daemon64.exe

IF "%1"=="-console" (
    "%java_bin_accepted%" -cp %APP_CLASSPATH% %JAVA_ARGS% %MAIN_CLASS% %APP_ARGS%
) ELSE IF "%1"=="-run" (
    "%APP_BIN_DIR%\%TargetExe%" -debug
) ELSE IF %1==-start (
    net start ${config.displayName}
) ELSE IF %1==-stop (
    net stop ${config.displayName}
) ELSE IF %1==-install (
    "%APP_BIN_DIR%\%TargetExe%" -install
) ELSE IF %1==-uninstall (
    "%APP_BIN_DIR%\%TargetExe%" -remove
) ELSE (
    echo Error unsupported argument
    GOTO PrintUsageJSLWin
)

GOTO endLabel

:PrintUsageJSLWin
REM annoying string escaping
echo Usage: ^[-^c^o^n^s^ole^|^-^r^u^n^|-^s^t^a^r^t^|-^s^t^o^p^|^-^ins^tall^|^-^un^inst^a^ll^]
exit /b

GOTO endLabel
