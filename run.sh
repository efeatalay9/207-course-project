#!/bin/sh
# Build locally and launch from the repository so the default catalogue path resolves.
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$project_dir"
export MAVEN_USER_HOME="$project_dir/.mvn/user-home"
./mvnw -q -DskipTests package
exec java -jar target/course-search.jar "$@"
