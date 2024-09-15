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

cd /d %EAF_HOME%
java -Dfile.encoding=utf-8 -jar "%EAF_HOME%\eaf.jar" %ALL_ARGS% -noansi