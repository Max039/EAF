@echo off

:: Create a string to hold all arguments
setlocal enabledelayedexpansion
set "ALL_ARGS="

:: Concatenate all arguments into a single string
:concat
if "%~1"=="" goto after_concat
set "ALL_ARGS=!ALL_ARGS! %1"
shift
goto concat

:after_concat
endlocal & set ALL_ARGS=%ALL_ARGS%

:: Continue with the rest of your script
echo Number of arguments: %*
echo Arguments: %ALL_ARGS%

:: BatchGotAdmin
:-------------------------------------
REM  --> Check for permissions
    IF "%PROCESSOR_ARCHITECTURE%" EQU "amd64" (
>nul 2>&1 "%SYSTEMROOT%\SysWOW64\cacls.exe" "%SYSTEMROOT%\SysWOW64\config\system"
) ELSE (
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
)

REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params= 
    echo UAC.ShellExecute "cmd.exe", "/c ""%~s0"" %params:"=""%", "", "runas", 0 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    pushd "%CD%"
    CD /D "%~dp0"

:: At this point, the script is running with admin privileges
echo Elevated Script Arguments: %ALL_ARGS%
cd /d %EAF_HOME%
java -Dfile.encoding=utf-8 -jar "%EAF_HOME%\eaf.jar" %ALL_ARGS%