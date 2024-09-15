# Disclaimer:

To use the following commands on macOS, you need to set up environment variables:

## Update Your Shell Environment

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

## Prepare Script

1. Restart Terminal (Cmd + Alt + Esc).
2. Change directory and make the script executable:
    ```bash
    cd $EAF_HOME/bin
    chmod +x eaf
    ```
3. Restart Terminal again (Cmd + Alt + Esc).

## Commands

- To run EAF:
    ```bash
    eaf "relative/full eaf-file path"
    ```
- To run EAF without GUI:
    ```bash
    eaf "relative/full eaf-file path" -nogui
    ```
