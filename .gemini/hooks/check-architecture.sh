#!/usr/bin/env bash
# Remmi Architecture Verification Hook
# Verifies that package boundaries and architecture constraints are respected.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "==> Running Remmi Architecture Verification..."

cd "$PROJECT_ROOT"

if [ -f "./gradlew" ]; then
    ./gradlew :app:testDebugUnitTest --tests "com.remmi.app.ArchitectureTest" --quiet
    echo "==> Architecture Verification Passed!"
else
    echo "ERROR: gradlew script not found in project root."
    exit 1
fi
