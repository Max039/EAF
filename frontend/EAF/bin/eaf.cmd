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

:: Check if an argument was provided
if "%~1"=="" (
    echo No file provided.
    exit /b 1
)

:: Check if the provided file ends with .eaf
set "file=%~1"
if /i not "%file:~-4%"==".eaf" (
    echo The file must end with .eaf
    exit /b 1
)

:: Check if the file exists
if not exist "%file%" (
    echo File %file% not found.
    exit /b 1
)

:: Get the absolute path of the file
for %%F in ("%file%") do set "abs_path=%%~fF"

:: Run the eaf.bat file with the absolute path of the file
"%eaf_bat_path%" "%abs_path%"

endlocal
