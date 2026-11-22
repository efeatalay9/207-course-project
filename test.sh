#!/bin/sh
# Run the complete fixture-based test suite without requiring a separate Maven install.
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$project_dir"
export MAVEN_USER_HOME="$project_dir/.mvn/user-home"
exec ./mvnw -B -ntp test "$@"
