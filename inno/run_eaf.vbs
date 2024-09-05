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

