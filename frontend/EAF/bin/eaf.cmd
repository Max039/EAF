@echo off
setlocal

:: Check if EAF_HOME is defined
if "%EAF_HOME%"=="" (
    echo EAF_HOME is not defined.
    exit /b 1
)

:: Define the full path to eaf.bat
set "eaf_bat_path=%EAF_HOME%\cmd.bat"

:: Check if the cmd.bat file exists
if not exist "%eaf_bat_path%" (
    echo cmd.bat not found at %eaf_bat_path%
    exit /b 1
)

:: Check if the first argument was provided
if "%~1"=="" (
    echo No file provided.
    exit /b 1
)

:: Check if the provided file has an accepted extension (.eaf, .mll, .generator, .ol)
set "file=%~1"

if /i "%file:~-4%"==".eaf" (
    goto :file_ok
) else if /i "%file:~-4%"==".mll" (
    goto :file_ok
) else if /i "%file:~-3%"==".ol" (
    goto :file_ok
) else if /i "%file:~-10%"==".generator" (
    goto :file_ok
) else (
    echo The file must end with .eaf, .generator, .mll, or .ol
    exit /b 1
)

:file_ok

:: Check if the file exists
if not exist "%file%" (
    echo File %file% not found.
    exit /b 1
)

:: Get the absolute path of the file
for %%F in ("%file%") do set "abs_path=%%~fF"

:: Initialize a variable to hold additional arguments
set "additional_args="

:: Loop through all arguments starting from %2 and build the additional_args string
shift
:loop
if "%~1"=="" goto endloop
set "additional_args=%additional_args% %1"
shift
goto loop
:endloop

:: Run the eaf.bat file with the absolute path of the file and remaining arguments
"%eaf_bat_path%" "%abs_path%" %additional_args%

endlocal
