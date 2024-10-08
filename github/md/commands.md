## Commands

![](../gifs/console.gif)

- To run EAF (accepts .eaf, .generator, .mll, .ol):
    ```bash
    eaf "file-path"
    ```
- To run without GUI:
    ```bash
    -nogui
	```
	
- To run with the full log enabled:
    ```bash
    -fulllog
	```
- To run without ansi encoding (when running via terminal default enabled):
    ```bash
    -noansi
	```
	
- To convert a .generator, .mll, .ol to a .eaf file in the same folder:
    ```bash
    -convert
	```

![](../gifs/convert.gif)
	
- To copy a version into eaf installation folder/project (can be used in conjunction with -convert to directly move the eaf into projects):
    ```bash
    -import
	```
	
- To enable further EvoAl logging:
    ```bash
    -debug
	```
	
- To run with the sudo password not needing to input it later:
    ```bash
    -sudopwd=PASSWORD
	```

# Disclaimer:

To use the following commands on macOS, you need to set up environment variables:

## MAC ONLY Update Your Shell Environment

1. Open your `.zshenv` file:
    ```bash
    vi ~/.zshenv
    ```
2. Start editing:
    - Press `i` to start editing.
3. Add the following lines to the file:
    ```bash
    export EAF_HOME="PATH_TO_EAF"  # After the first start, it can be found in Documents -> Eaf
    export PATH=$PATH:$EAF_HOME/bin
    ```
4. Save and exit the file:
    - Press `Esc` to stop editing.
    - Type `:w` and press `Enter` to save.
    - Type `:q` and press `Enter` to quit.

1. Restart Terminal (Cmd + Alt + Esc).
2. Change directory and make the script executable:
    ```bash
    cd $EAF_HOME/bin
    chmod +x eaf
    ```
3. Restart Terminal again (Cmd + Alt + Esc).

  
