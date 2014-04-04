@ECHO OFF

set bit64=n
if /I %Processor_Architecture%==AMD64 set bit64=y
if /I "%PROCESSOR_ARCHITEW6432%"=="AMD64" set bit64=y

echo bit64: %bit64%