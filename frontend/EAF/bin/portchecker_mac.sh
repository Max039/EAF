#!/bin/zsh

# Define host and port for the JVM debugger
host="localhost"
port=1044
timeout=60  # Set the timeout to 60 seconds, adjust as needed

echo "Waiting for $host:$port to open..."

# Function to check if the port is open using netcat (nc)
check_port() {
  nc -z "$host" "$port" >/dev/null 2>&1
}

# Wait for the debugger port to become available
for ((i=1; i<=timeout; i++)); do
  if check_port; then
    echo "Port $port is open, starting the debugger..."
    break
  fi
  echo "Port $port not open yet... retrying ($i/$timeout)"
  sleep 1
done

# If the port wasn't opened within the timeout, exit with an error
if ! check_port; then
  echo "Timeout reached. Port $port is still not open. Exiting."
  exit 1
fi

# Now start your remote JVM debugger (this part happens after the port is ready)
echo "Starting JVM remote debugger..."

# Add your remote JVM debugger command here
# Example placeholder (replace this with actual debugger start command):
# intellij-debugger --remote --host "$host" --port "$port"