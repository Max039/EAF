If Not WScript.Arguments.Named.Exists("elevated") Then
    CreateObject("Shell.Application").ShellExecute "wscript.exe", """" & WScript.ScriptFullName & """ /elevated", "", "runas", 1
    WScript.Quit
End If

Set WshShell = CreateObject("WScript.Shell")

' Initialize a variable to hold the arguments string
Dim args, argsString
argsString = ""

' Iterate through all arguments and build the argument string
For i = 0 To WScript.Arguments.Count - 1
    argsString = argsString & chr(34) & WScript.Arguments(i) & chr(34) & " "
Next

' Trim any trailing space from the argument string
argsString = Trim(argsString)

' Try to expand the EAF_HOME environment variable
Dim eafHome
eafHome = WshShell.ExpandEnvironmentStrings("%EAF_HOME%")

' Check if the environment variable is found
If eafHome = "%EAF_HOME%" Then
    MsgBox "EAF_HOME environment variable not found!"
    WScript.Quit
End If

' Run the batch file using the expanded EAF_HOME path
WshShell.Run chr(34) & eafHome & "\eaf.bat" & chr(34) & " " & argsString, 0

Set WshShell = Nothing
