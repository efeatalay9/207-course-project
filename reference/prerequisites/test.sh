#!/usr/bin/env bash
set -euo pipefail

prototype_dir=$(cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(cd -- "$prototype_dir/../.." && pwd)
gson_relative=com/google/code/gson/gson/2.13.2/gson-2.13.2.jar
prereq_gson_jar=${PREREQ_GSON_JAR:-"$project_dir/.mvn/repository/$gson_relative"}
if [[ -z ${PREREQ_GSON_JAR:-} && ! -r "$prereq_gson_jar" ]]; then
    prereq_gson_jar="$HOME/.m2/repository/$gson_relative"
fi
if [[ ! -r "$prereq_gson_jar" ]]; then
    printf '%s\n' 'Run ./test.sh at the repo root to cache Gson, or set PREREQ_GSON_JAR to an installed Gson JAR.' >&2
    exit 1
fi
if [[ -n ${PREREQ_JAVA_HOME:-} ]]; then
    prereq_java_home=$PREREQ_JAVA_HOME
elif [[ -x /usr/libexec/java_home ]]; then
    prereq_java_home=$(/usr/libexec/java_home -v 17)
else
    prereq_java_home=$(dirname -- "$(dirname -- "$(readlink -f "$(command -v javac)")")")
fi

prereq_build=$(mktemp -d)
trap 'rm -r -- "$prereq_build"' EXIT
python3 -B "$prototype_dir/test_compiler.py" "$prototype_dir/prerequisites.json" "$prereq_build/updated.json"
"$prereq_java_home/bin/javac" --release 17 --add-modules jdk.httpserver -cp "$prereq_gson_jar" \
    -d "$prereq_build" "$prototype_dir/PrerequisiteCatalog.java" "$prototype_dir/PrerequisiteTest.java"
"$prereq_java_home/bin/java" --add-modules jdk.httpserver -cp "$prereq_build:$prereq_gson_jar" \
    PrerequisiteTest "$prototype_dir/prerequisites.json" "$prereq_build/updated.json" "$@"
