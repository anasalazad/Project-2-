#!/usr/bin/env bash
# Compiles (main, unit tests, device tests) and runs unit tests for every staged tag.
cd "$(dirname "$0")"
S=$(pwd)
: > check-results.txt
for t in $(git -C staged tag | sort); do
  git -C stagecheck checkout -q -f "$t" && git -C stagecheck clean -qfdx
  for attempt in 1 2 3; do
    (cd check && timeout 900 /opt/gradle/bin/gradle compileKotlin compileTestKotlin compileDeviceTestKotlin test -PappRoot="$S/stagecheck" --max-workers=1 -q > "$S/logs-$t.txt" 2>&1)
    rc=$?
    grep -q "429\|Could not GET\|Could not download" "$S/logs-$t.txt" || break
    sleep 20
  done
  tests=$(cat check/build/test-results/test/*.xml 2>/dev/null | grep -o 'tests="[0-9]*"' | grep -o '[0-9]*' | paste -sd+ | bc 2>/dev/null)
  echo "$t rc=$rc tests=${tests:-0}" | tee -a check-results.txt
  rm -rf check/build/test-results
done
echo DONE >> check-results.txt
