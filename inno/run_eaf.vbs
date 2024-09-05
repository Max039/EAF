Set WshShell = CreateObject("WScript.Shell")
If WScript.Arguments.Count > 0 Then
    filePath = WScript.Arguments(0)
    WshShell.Run chr(34) & "C:\Program Files (x86)\EvoAl Frontend\eaf.bat" & chr(34) & " OPEN=" & chr(34) & filePath & chr(34), 0
Else
    WshShell.Run chr(34) & "C:\Program Files (x86)\EvoAl Frontend\eaf.bat" & chr(34), 0
End If
Set WshShell = Nothing
