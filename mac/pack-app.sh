#!/bin/bash

# Variables
APP_PATH="./Eaf.app"
MACOS_FOLDER="$APP_PATH/Contents/MacOS"
SOURCE_FOLDER="../frontend/EAF"
LAUNCHER_FILE="launcher"

# Check if the target MacOS folder exists and delete it
if [ -d "$MACOS_FOLDER" ]; then
    echo "Deleting existing MacOS folder at $MACOS_FOLDER"
    rm -rf "$MACOS_FOLDER"
else
    echo "No MacOS folder found at $MACOS_FOLDER, nothing to delete."
fi

# Create the MacOS directory if it doesn't exist
echo "Creating MacOS directory at $MACOS_FOLDER"
mkdir -p "$MACOS_FOLDER"

# Copy the new folder into the MacOS directory
echo "Copying $SOURCE_FOLDER to $MACOS_FOLDER"
cp -R "$SOURCE_FOLDER/"* "$MACOS_FOLDER"

# Copy the launcher.exec to the MacOS directory
echo "Adding $LAUNCHER_FILE to $MACOS_FOLDER"
cp "$LAUNCHER_FILE" "$MACOS_FOLDER"

echo "Operation completed successfully!"


# Define paths
EAF_APP="Eaf.app"
TEMPLATE_DMG="Eaf-Installer-Template.dmg"
FINAL_DMG="Eaf-Installer.dmg"
MOUNT_POINT="/Volumes/Eaf-Installer-Template"

# Check if the final DMG already exists and delete it
if [ -f "$FINAL_DMG" ]; then
    echo "Deleting existing $FINAL_DMG..."
    rm "$FINAL_DMG"
fi

# Mount the template DMG
echo "Mounting $TEMPLATE_DMG..."
hdiutil attach "$TEMPLATE_DMG" -mountpoint "$MOUNT_POINT"

# Ensure the DMG is mounted
if [ ! -d "$MOUNT_POINT/$EAF_APP" ]; then
    echo "Error: Failed to mount $TEMPLATE_DMG or $EAF_APP does not exist in the DMG."
    exit 1
fi

# Remove the contents of the Eaf.app in the mounted DMG
echo "Removing old contents of $EAF_APP in the template..."
rm -rf "$MOUNT_POINT/$EAF_APP/Contents"

# Copy the new Eaf.app contents to the mounted DMG
echo "Copying new contents to $MOUNT_POINT/$EAF_APP..."
cp -R "$EAF_APP/Contents" "$MOUNT_POINT/$EAF_APP/"

# Unmount the DMG
echo "Unmounting $TEMPLATE_DMG..."
hdiutil detach "$MOUNT_POINT"

# Create a new read-only DMG
echo "Creating $FINAL_DMG..."
hdiutil convert "$TEMPLATE_DMG" -format UDZO -o "$FINAL_DMG"

echo "Process completed successfully."
