#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$PROJECT_DIR/.gradle-user-home}"

cd "$PROJECT_DIR"
./gradlew test "$@"
