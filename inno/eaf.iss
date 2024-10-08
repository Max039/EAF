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
UninstallDisplayIcon={#SourcePath}\eaf.ico
SetupIconFile={#SourcePath}\eaf.ico

#define HideConsole True

[Files]
; Copy the entire folder to the selected directory
Source: "{#SourcePath}\..\frontend\EAF\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs; Excludes: "builds,session.cache,projects,.gitignore"; 

Source: "{#SourcePath}\vbs.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\run_eaf.vbs"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\eaf.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SourcePath}\cmd.bat"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\projects";

[Icons]
; Create a shortcut for the executable in the start menu with the icon
Name: "{group}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{#SourcePath}\eaf.ico"
; Optionally, create a shortcut on the desktop with the icon
Name: "{userdesktop}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{#SourcePath}\eaf.ico"; Tasks: desktopicon
Name: "{app}\EvoAl Frontend"; Filename: "{app}\run_eaf.vbs"; IconFilename: "{#SourcePath}\eaf.ico"; WorkingDir: "{app}"
  


[Tasks]
; Option to create a desktop shortcut
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"; Flags: unchecked

[Registry]
; Register the .eaf file extension with your application
Root: HKCR; Subkey: ".eaf"; ValueType: string; ValueName: ""; ValueData: "EvoAl Frontend File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "EvoAl Frontend File"; ValueType: string; ValueName: ""; ValueData: "EvoAl Frontend File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl Frontend File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{#SourcePath}\eaf.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "EvoAl Frontend File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\vbs.bat"" ""%1"""; Flags: uninsdeletekey

Root: HKCR; Subkey: ".generator"; ValueType: string; ValueName: ""; ValueData: "Generator File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Generator File"; ValueType: string; ValueName: ""; ValueData: "Generator File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Generator File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{#SourcePath}\generator.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Generator File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\vbs.bat"" ""%1"""; Flags: uninsdeletekey


Root: HKCR; Subkey: ".mll"; ValueType: string; ValueName: ""; ValueData: "Machine Learning Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Machine Learning Language File"; ValueType: string; ValueName: ""; ValueData: "Machine Learning Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Machine Learning Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{#SourcePath}\mll.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Machine Learning Language File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\vbs.bat"" ""%1"""; Flags: uninsdeletekey


Root: HKCR; Subkey: ".ol"; ValueType: string; ValueName: ""; ValueData: "Optimization Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Optimization Language File"; ValueType: string; ValueName: ""; ValueData: "Optimization Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Optimization Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{#SourcePath}\ol.ico"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Optimization Language File\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\vbs.bat"" ""%1"""; Flags: uninsdeletekey


Root: HKCR; Subkey: ".ddl"; ValueType: string; ValueName: ""; ValueData: "Data Definition Language File"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "Data Definition Language File"; ValueType: string; ValueName: ""; ValueData: "Data Definition Language File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "Data Definition Language File\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{#SourcePath}\ddl.ico"; Flags: uninsdeletekey


[Code]
function DeleteDirectory(const Dir: String): Boolean;
begin
  Result := DelTree(Dir, True, True, True);
end;

procedure DeleteDirectories;
var
  BasePath: String;
begin
  BasePath := ExpandConstant('{app}');
  
  DeleteDirectory(BasePath + '\src') 
  
  DeleteDirectory(BasePath + '\target')
  
  DeleteDirectory(BasePath + '\.idea')
   
end;



// Declare necessary constants and function from Windows API
const
  WM_SETTINGCHANGE = $1A;
  SMTO_ABORTIFHUNG = 2;


procedure AddToPath;
var
  OldPath, NewPath: string;
begin
  // Get the current PATH environment variable
  if RegQueryStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', OldPath) then
  begin
    // Check if our path is already there
    if Pos(ExpandConstant(';{app}\bin'), OldPath) = 0 then
    begin
      // Add the application's installation folder to the system PATH
      NewPath := OldPath + ExpandConstant(';{app}\bin');
      RegWriteStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', NewPath);

    end;
  end;
end;


procedure AddHome;
var
  NewPath: string;
begin
  // System Environment Variable (HKLM) - Available to all users
  NewPath := ExpandConstant('{app}');
  RegWriteStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'EAF_HOME', NewPath);

  // User Environment Variable (HKCU) - Available only to the current user
  RegWriteStringValue(HKCU, 'Environment', 'EAF_HOME', NewPath);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    // Add the application path to the PATH variable after installation
    AddToPath;
    AddHome;
    DeleteDirectories;
  end;
end;




[Run]
; Create a VBS script to run the batch file
;Filename: "{app}\vbs.bat"; Description: "Run EvoAl Frontend"; Flags: nowait postinstall skipifsilent
