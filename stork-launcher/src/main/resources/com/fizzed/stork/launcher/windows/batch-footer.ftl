:endLabel
goto :eof

:errorlabel
setlocal
SET error_code=%~1
IF "%error_code%" NEQ "" EXIT /B %error_code
Exit /B %ERRORLEVEL%
goto :eof
