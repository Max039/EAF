#!/bin/sh
export DISPLAY=localhost:0.0
export EVOAL_HOME=$( cd -- "$(dirname $0)/../../builds/20240731-120630/evoal" >/dev/null 2>&1 ; pwd -P )
# Print the current working directory for debugging
echo "Current working directory in second script: $(pwd)"
echo "EVOAL_HOME in second script: $EVOAL_HOME"
export EVOAL_JVM_ARGUMENTS=""
# Get the directory of the currently executed script
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
# Change the working directory to the script's directory
cd "$SCRIPT_DIR"
$SHELL $EVOAL_HOME/bin/evoal-search.sh . config.ol output
