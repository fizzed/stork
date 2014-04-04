@ECHO OFF

(set | find "ProgramFiles(x86)" > NUL) && (echo "%ProgramFiles(x86)%" | find "x86") > NUL && set bits=64 || set bits=32

echo bits: %bits%