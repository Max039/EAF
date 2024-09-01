[Setup]
; General settings
AppName=EvoAl Frontend
AppVersion=1.0
DefaultDirName={pf}\EvoAl Frontend\
DefaultGroupName=EvoAl Frontend
OutputBaseFilename=eaf-setup
Compression=lzma
SolidCompression=yes
UninstallDisplayIcon={app}\icon.ico
SetupIconFile={#SourcePath}\icon.ico

#define HideConsole True

[Files]
; Copy the entire folder to the selected directory
Source: "{#SourcePath}\..\frontend\EAF\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs; Excludes: "builds,session.cache";
Source: "{#SourcePath}\icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\run_eaf.vbs"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\eaf.bat"; DestDir: "{app}"; Flags: ignoreversion
;Source: "{#SourcePath}\first_time.bat"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Create a shortcut for the executable in the start menu with the icon
Name: "{group}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\icon.ico"
; Optionally, create a shortcut on the desktop with the icon
Name: "{userdesktop}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon
Name: "{app}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\icon.ico"; WorkingDir: "{app}"
  


[Tasks]
; Option to create a desktop shortcut
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"; Flags: unchecked

[Registry]
; Register the .eaf file extension with your application
Root: HKCR; Subkey: ".eaf"; ValueType: string; ValueName: ""; ValueData: "EvoAl-Frontend-File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "EvoAl-Frontend-File"; ValueType: string; ValueName: ""; ValueData: "EvoAl-Frontend-File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl-Frontend-File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\icon.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl-Frontend-File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\run_eaf.vbs"" ""%1"""; Flags: uninsdeletekey


[Run]
; Create a VBS script to run the batch file
;Filename: "{app}\first_time.bat"; Description: "Run EvoAl Frontend"; Flags: nowait postinstall skipifsilent

[Code]
