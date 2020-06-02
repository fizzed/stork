
@REM
@REM for testing this independently (in your shell)
@REM   echo off
@REM   setlocal ENABLEDELAYEDEXPANSION
@REM   set MIN_JAVA_VERSION=1.8
@REM   JAVA_SEARCH_DEBUG=1
@REM

if NOT "%JAVA_EXE%"=="" (
    set java_bin_accepted=%JAVA_EXE%
    goto :AcceptableJavaBinFound
)

set target_java_ver_num=0
call :ExtractJavaMajorVersionNum "%MIN_JAVA_VERSION%" target_java_ver_num
if "%target_java_ver_num%"=="0" (
    echo Unable to extract major version from "%MIN_JAVA_VERSION%"
    Exit /B 1
)

set target_java_ver_max=0
call :ExtractJavaMajorVersionNum "%MAX_JAVA_VERSION%" target_java_ver_max

call :JavaSearchDebug "target_java_ver_num: %target_java_ver_num%"
call :JavaSearchDebug "target_java_ver_max: %target_java_ver_max%"

@REM
@REM is java or jre in JAVA_HOME acceptable?
@REM
call :JavaSearchDebug "Searching JAVA_HOME env var..."
if NOT "%JAVA_HOME%"=="" (
    call :IsJavaBinVersionAcceptable "!JAVA_HOME!\jre\bin\java" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
    if NOT "!java_bin_accepted!" == "" goto :AcceptableJavaBinFound

    call :IsJavaBinVersionAcceptable "!JAVA_HOME!\bin\java" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
    if NOT "!java_bin_accepted!" == "" goto :AcceptableJavaBinFound
)


@REM
@REM is java in the current path?
@REM
call :JavaSearchDebug "Searching PATH..."
for %%X in (java.exe) do (set JAVA_IN_PATH=%%~$PATH:X)
IF DEFINED JAVA_IN_PATH (
    call :IsJavaBinVersionAcceptable !JAVA_IN_PATH! !target_java_ver_num! !target_java_ver_max! java_bin_accepted
    if NOT "!java_bin_accepted!" == "" goto :AcceptableJavaBinFound
)


@REM
@REM query registry for java runtime environment
@REM no keys will log to stderr so we redirect to nul
@REM

set reg_best_java_bin=


call :JavaSearchDebug "Searching registry for JDK 9+ entries..."

@REM always sorted most recent first
set reg_java_bin=
for /f "tokens=2*" %%i in ('reg query "HKLM\Software\JavaSoft\JDK" /s 2^>nul ^| find "JavaHome"') do if not defined reg_java_bin (
    set reg_java_bin=%%j\bin\java
)

call :IsJavaBinVersionAcceptable "!reg_java_bin!" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
if NOT "!java_bin_accepted!" == "" set reg_best_java_bin=!java_bin_accepted!

if NOT "%reg_best_java_bin%"=="" (
    set java_bin_accepted=!reg_best_java_bin!
    goto :AcceptableJavaBinFound
)


call :JavaSearchDebug "Searching registry for JDK 8 or less entries..."

@REM always sorted most recent first
set reg_java_bin=
for /f "tokens=2*" %%i in ('reg query "HKLM\Software\JavaSoft\Java Development Kit" /s 2^>nul ^| find "JavaHome"') do if not defined reg_java_bin (
    set reg_java_bin=%%j\bin\java
)

call :IsJavaBinVersionAcceptable "!reg_java_bin!" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
if NOT "!java_bin_accepted!" == "" set reg_best_java_bin=!java_bin_accepted!

if NOT "%reg_best_java_bin%"=="" (
    set java_bin_accepted=!reg_best_java_bin!
    goto :AcceptableJavaBinFound
)


call :JavaSearchDebug "Searching registry for JRE 9+ entries..."

@REM always sorted most recent first
set reg_java_bin=
for /f "tokens=2*" %%i in ('reg query "HKLM\Software\JavaSoft\JRE" /s 2^>nul ^| find "JavaHome"') do if not defined reg_java_bin (
    set reg_java_bin=%%j\bin\java
)

call :IsJavaBinVersionAcceptable "!reg_java_bin!" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
if NOT "!java_bin_accepted!" == "" set reg_best_java_bin=!java_bin_accepted!

if NOT "%reg_best_java_bin%"=="" (
    set java_bin_accepted=!reg_best_java_bin!
    goto :AcceptableJavaBinFound
)


call :JavaSearchDebug "Searching registry for JRE 8 or less entries..."

@REM always sorted most recent first
set reg_java_bin=
for /f "tokens=2*" %%i in ('reg query "HKLM\Software\JavaSoft\Java Runtime Environment" /s 2^>nul ^| find "JavaHome"') do if not defined reg_java_bin (
    set reg_java_bin=%%j\bin\java
)

call :IsJavaBinVersionAcceptable "!reg_java_bin!" !target_java_ver_num! !target_java_ver_max! java_bin_accepted
if NOT "!java_bin_accepted!" == "" set reg_best_java_bin=!java_bin_accepted!

if NOT "%reg_best_java_bin%"=="" (
    set java_bin_accepted=!reg_best_java_bin!
    goto :AcceptableJavaBinFound
)


:NoAcceptableJavaBinFound
@REM if we get here then the search above failed
call :JavaSearchDebug "No acceptable java found"
@ECHO Unable to find Java runtime on system with version ^>^= %MIN_JAVA_VERSION%
@ECHO Please visit http://java.com to install an acceptable Java Runtime
@ECHO For debugging how this script searches for the JRE:
@ECHO   SET LAUNCHER_DEBUG=1
@ECHO and then run the command again
EXIT /B 1

:AcceptableJavaBinFound
call :JavaSearchDebug "Acceptable java bin found: %java_bin_accepted%"
goto :JavaSearchEnd


@REM 1.7 -> returns 7 in param 2 or 0 if not found
:ExtractJavaMajorVersionNum
setlocal
SET full_ver=%~1
for /f "delims=. tokens=1-2" %%v in ("%full_ver%") do (
    @REM @echo Major: %%v
    @REM @echo Minor: %%w
    if %%v gtr 1 (
        set maj_ver_num=%%v
    ) else (
        set maj_ver_num=%%w
    )
)
if "%maj_ver_num%"=="" (
    set maj_ver_num=0
)
( endlocal
    set "%2=%maj_ver_num%"
)
GOTO:EOF


@REM java_exe -> returns major version like 7 in param 2
:GetJavaBinMajorVersionNum
setlocal
SET java_bin=%~1

@REM echo getting ver for: %java_bin%

for /f "tokens=3" %%g in ('cmd /c "%java_bin%" -version 2^>^&1 ^| findstr /i "version"') do (
    REM @echo Output: %%g
    set JAVAVER=%%g
)
SET java_version=
if NOT "%JAVAVER%"=="" (
    set JAVAVER=%JAVAVER:"=%
    @REM @echo Output: %JAVAVER%
    for /f "delims=. tokens=1-3" %%v in (%JAVAVER%) do (
        @REM @echo Major: %%v
        @REM @echo Minor: %%w
        @REM @echo Build: %%x
        if %%v gtr 1 (
            @REM @echo was greater than 1
            set java_version=%%v
        ) else (
            @REM @echo was equal to 1
            set java_version=%%w
        )
    )
)
( endlocal
    @REM @echo java_version: %java_version%
    set "%2=%java_version%"
    set "%3=%JAVAVER%"
)
GOTO :EOF


@REM call :IsJavaBinVersionAcceptable java_bin target_java_ver_num target_java_ver_max java_bin_if_accepted
:IsJavaBinVersionAcceptable
setlocal
SET java_bin=%~1
SET target_java_ver_num=%~2
SET target_java_ver_max=%~3
@REM call :JavaSearchDebug "java_bin_ver_num: %java_bin_ver_num%"
@REM call :JavaSearchDebug "java_bin_ver_max: %java_bin_ver_max%"
call :JavaSearchDebug "java_bin: %java_bin%"
call :GetJavaBinMajorVersionNum "%java_bin%" java_bin_ver_num java_full_ver
if "%java_bin_ver_num%"=="" (
    set java_bin_ver_num=0
)
if %java_bin_ver_num% geq %target_java_ver_num% (
    if %target_java_ver_max% gtr 0 (
        if %java_bin_ver_num% leq %target_java_ver_max% (
            set java_bin_if_accepted=!java_bin!
            call :JavaSearchDebug " version: !java_full_ver! ^(meets minimum 1.!target_java_ver_num! and maximum !target_java_ver_max!^)"
        ) else (
            set java_bin_if_accepted=
            call :JavaSearchDebug " version: !java_full_ver! ^(meets minimum 1.!target_java_ver_num! but NOT maximum !target_java_ver_max!^)"
        )
    ) else (
        set java_bin_if_accepted=!java_bin!
        call :JavaSearchDebug " version: !java_full_ver! ^(meets minimum 1.!target_java_ver_num!^)"
    )
) else (
    set java_bin_if_accepted=
    call :JavaSearchDebug " version: !java_full_ver! ^(^less than 1.!target_java_ver_num! though^)"
)
( endlocal
    set "%4=%java_bin_if_accepted%"
)
GOTO :EOF


@REM Strip quotes and extra backslash from string
:StripQuotesAndBackslash
setlocal
set o=%~1
SET n=%~1
SET n=%n:\\=\%
SET n=%n:"=%
( endlocal
    IF NOT "%n%"=="" set "%2=%n%" ELSE set "%2=%o%"
)
GOTO :EOF


:JavaSearchDebug
setlocal
SET v=%~1
if "%LAUNCHER_DEBUG%"=="1" (
    echo ^[JAVA_SEARCH^] !v!
)
GOTO :EOF

:JavaSearchEnd
