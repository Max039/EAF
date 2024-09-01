

Set WshShell = CreateObject("WScript.Shell")
WshShell.Run chr(34) & "C:\Program Files (x86)\EvoAl Frontend\eaf.bat" & chr(34), 0
Set WshShell = Nothing