[Setup]
; General settings
AppName=EvoAl Frontend
AppVersion=1.0
;DefaultDirName={pf}\EvoAl Frontend\
DefaultDirName=C:\EvoAl\Eaf\
DefaultGroupName=EvoAl Frontend
OutputBaseFilename=eaf-setup
Compression=lzma
SolidCompression=yes
UninstallDisplayIcon={app}\eaf.ico
SetupIconFile={#SourcePath}\eaf.ico

#define HideConsole True

[Files]
; Copy the entire folder to the selected directory
Source: "{#SourcePath}\..\frontend\EAF\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs; Excludes: "builds,session.cache";
Source: "{#SourcePath}\eaf.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\mll.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\ol.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\ddl.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\generator.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\vbs.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\run_eaf.vbs"; DestDir: "{app}"; Flags: ignoreversion; AfterInstall: FinishVbs
Source: "{#SourcePath}\eaf.bat"; DestDir: "{app}"; Flags: ignoreversion; AfterInstall: AfterInstall


[Icons]
; Create a shortcut for the executable in the start menu with the icon
Name: "{group}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\eaf.ico"
; Optionally, create a shortcut on the desktop with the icon
Name: "{userdesktop}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\eaf.ico"; Tasks: desktopicon
Name: "{app}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{app}\eaf.ico"; WorkingDir: "{app}"
  


[Tasks]
; Option to create a desktop shortcut
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"; Flags: unchecked

[Registry]
; Register the .eaf file extension with your application
Root: HKCR; Subkey: ".eaf"; ValueType: string; ValueName: ""; ValueData: "EvoAl Frontend File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "EvoAl Frontend File"; ValueType: string; ValueName: ""; ValueData: "EvoAl Frontend File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl Frontend File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\eaf.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl Frontend File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\vbs.bat"" ""%1"""; Flags: uninsdeletekey

Root: HKCR; Subkey: ".generator"; ValueType: string; ValueName: ""; ValueData: "Generator File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Generator File"; ValueType: string; ValueName: ""; ValueData: "Generator File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Generator File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\generator.ico"; Flags: uninsdeletekey

Root: HKCR; Subkey: ".mll"; ValueType: string; ValueName: ""; ValueData: "Machine Learning Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Machine Learning Language File"; ValueType: string; ValueName: ""; ValueData: "Machine Learning Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Machine Learning Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\mll.ico"; Flags: uninsdeletekey

Root: HKCR; Subkey: ".ol"; ValueType: string; ValueName: ""; ValueData: "Optimization Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Optimization Language File"; ValueType: string; ValueName: ""; ValueData: "Optimization Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Optimization Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\ol.ico"; Flags: uninsdeletekey

Root: HKCR; Subkey: ".ddl"; ValueType: string; ValueName: ""; ValueData: "Data Definition Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Data Definition Language File"; ValueType: string; ValueName: ""; ValueData: "Data Definition Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Data Definition Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\ddl.ico"; Flags: uninsdeletekey


[Code]
procedure AfterInstall();
var
  FileName: string;
  FileContent: TStringList;
begin
  FileName := ExpandConstant('{app}\eaf.bat');
  
  // Load the file into TStringList
  FileContent := TStringList.Create;
  try
    FileContent.LoadFromFile(FileName);
    FileContent.Add('cd /d ' + ExpandConstant('{app}')); // Change to /d for drive change support
    // Add lines to the file
    FileContent.Add('java -jar "' + ExpandConstant('{app}\eaf.jar') + '" %ALL_ARGS%');

    
    // Save the changes back to the file
    FileContent.SaveToFile(FileName);
  finally
    FileContent.Free;
  end;
end;

procedure FinishVbs();
var
  FileName: string;
  FileContent: TStringList;
begin
  FileName := ExpandConstant('{app}\run_eaf.vbs');
  
  // Load the file into TStringList
  FileContent := TStringList.Create;
  try
    FileContent.LoadFromFile(FileName);
    FileContent.Add('WshShell.Run chr(34) & "' + ExpandConstant('{app}\eaf.bat') + '" & chr(34) & " " & argsString, 0'); // Change to /d for drive change support
    // Add lines to the file
    FileContent.Add('Set WshShell = Nothing');

    
    // Save the changes back to the file
    FileContent.SaveToFile(FileName);
  finally
    FileContent.Free;
  end;
end;

[Run]
; Create a VBS script to run the batch file
;Filename: "{app}\vbs.bat"; Description: "Run EvoAl Frontend"; Flags: nowait postinstall skipifsilent
