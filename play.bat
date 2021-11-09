@echo off
:while
set /p color="Enter color: "
java Chess %color%
goto :while