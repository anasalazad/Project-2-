# Patch generator (maintenance only)

Rebuilds `handover/patches/` and `handover/BUILD_PLAN.md` if the app code on this branch changes.
The scripts use absolute paths at the top (`SRC`, `SCR`); edit them for your environment.

1. **Pre-favourites versions:** copy the files touched by the favourites commit (`git show --name-only 0623e0b`)
   into `$SCR/prefav/`, then reverse-apply that commit there: `git show 0623e0b | git apply -R`.
2. `python3 stager.py`: replays all substages into `$SCR/staged` (one commit + tag per substage).
3. `python3 gradlecheck.py`: checks every stage's Gradle catalog references.
4. `checkall.sh`: compiles (main, unit tests, device tests) and runs unit tests for every stage via `tools/compile-check`.
5. Export one `git diff --binary --full-index -M` per tag into `handover/patches/<id>-<slug>.patch`, then write `patch-meta.json`.
6. `python3 gen_plan.py`: regenerates `BUILD_PLAN.md` from `plan_content.py` + `patch-meta.json`.
