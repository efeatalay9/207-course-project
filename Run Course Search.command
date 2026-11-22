#!/bin/sh
# Finder launcher for the macOS testing window.
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
"$project_dir/run.sh" "$@"
status=$?
if [ "$status" -ne 0 ]; then
    printf '\nLaunch failed. Press Enter to close this window.\n'
    read -r ignored
fi
exit "$status"
