#!/usr/bin/env bash
# Compiles the app sources and runs the unit tests without the Android SDK. See build.gradle.kts.
# Maven Central sometimes rate-limits (HTTP 429), so failed downloads are retried.
set -u
cd "$(dirname "$0")"
GRADLE="${GRADLE:-gradle}"
for attempt in 1 2 3 4; do
  "$GRADLE" test --no-daemon -q --max-workers=1 > build-output.txt 2>&1
  status=$?
  grep -q "429\|Could not GET\|Could not download" build-output.txt || break
  echo "Download problem, retrying ($attempt)..."; sleep $((attempt * 10))
done
grep "^e:" build-output.txt
for f in build/test-results/test/*.xml; do
  grep -o 'testsuite name="[^"]*" tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' "$f"
done 2>/dev/null
[ $status -eq 0 ] && echo "OK: compiled and all unit tests passed" || echo "FAILED: see build-output.txt"
exit $status
