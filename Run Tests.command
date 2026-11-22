#!/bin/sh
# Finder launcher: keep the terminal open so the test summary can be read.
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
"$project_dir/test.sh" "$@"
status=$?
printf '\nTests finished with exit code %s. Press Enter to close this window.\n' "$status"
read -r ignored
exit "$status"
