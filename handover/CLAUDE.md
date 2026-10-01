# CLAUDE.md: The Feline Co. (local rebuild)

**Local-only file. Never commit it.** It is excluded through `.git/info/exclude`.

## Who you're working with and why
- The owner is a **tutor** building an **"ideal submission"** for a university Android assignment, to show students how the work *should* be done. They are not graded; they want a realistic, high-quality, well-documented build.
- The app (Kotlin, Jetpack Compose, Room, Navigation Compose) is **already written**. It lives in `../feline-source` (a clone of the original repo) and is split into **65 ordered patches**. Your job is to **rebuild it here one substage at a time**, test each stage with the owner, keep the worklog, and give the owner the git commands to commit and push.
- Machine: MacBook Air M1, **8 GB RAM**. Test device: **Samsung tablet** (physical, USB).

## Hard rules
1. **Never run `git commit`, `git push`, `git reset`, `git checkout`, `git rebase`, `git stash` or anything else that changes history or the remote.** The owner commits and pushes. You *give* them the commands. (`.claude/settings.local.json` also blocks commit/push.) Read-only git (`status`, `diff`, `log`) and `git apply` are fine.
2. **Never commit local-only files:** `CLAUDE.md`, `WORKLOG.md`, `screenshots/`, `.claude/`. They're in `.git/info/exclude`; don't remove those lines, and don't add them to `.gitignore`.
3. Apply patches **in order** from `BUILD_PLAN.md`. Don't skip, reorder or merge substages unless the owner asks.
4. Never mention AI, Claude or prompts in **commit messages or committed files**. AI content belongs only in `WORKLOG.md`'s Gen AI sections.
5. Keep changes minimal. If something breaks, fix the smallest thing, log it in the worklog Issues Log, and tell the owner.

## Where things are
| What | Path |
|---|---|
| The plan (substages, patches, tests, screenshots, worklog notes, demo prompts) | `../feline-source/handover/BUILD_PLAN.md` |
| Patches | `../feline-source/handover/patches/` |
| README intro for step 00.1 | `../feline-source/handover/00.1-README.md` |
| Architecture, credit rules, decisions (reference) | `../feline-source/CONTEXT.md` |
| Assignment brief and rubric | `../feline-source/Specifications and requirements.md` |
| Photo/font shopping list | `../feline-source/ASSETS.md` |
| The finished app (read-only reference) | `../feline-source/app/` |
| **Worklog (the owner's report)** | `./WORKLOG.md` |
| Screenshots | `./screenshots/` (`S01.png`, `S02.png`, …) |

If `../feline-source` isn't there, ask the owner where they put it and use that path instead.

## Start of every session
1. Read `WORKLOG.md` §1.9 (status column) and the latest §2 entry to find where you are. Confirm with `git log --oneline -5`.
2. Tell the owner, in one line, which substage is next.

## The loop: run this for every substage
1. **Announce:** "Next: `NN.N` – <commit message>. Applying the patch."
2. **Apply:** `git apply --check --whitespace=nowarn ../feline-source/handover/patches/<file>` then the same without `--check`.
   - If it fails because of an earlier local fix: `git apply --reject --whitespace=nowarn …`, merge the `.rej` hunks by hand keeping the fix, delete the `.rej` files, and tell the owner.
3. **Build:** `./gradlew :app:assembleDebug` (skip for 01.1, which has no app module yet). From stage 3 on, also run `./gradlew :app:testDebugUnitTest` and report the number of tests passed.
   - If the build fails, read the error, fix it minimally, re-run, and log it as an Issue in the worklog.
4. **Mid-stage substage?** Give the commit commands (step 7) and continue with the next substage when the owner says so.
5. **Last substage of a stage?** Ask the owner to run the app on the tablet (Android Studio ▶) and go through the stage's **"Test this stage"** checklist from the plan. Ask them to take the listed screenshots and save them as `screenshots/Sxx.png`. Run instrumented tests if the stage has them: `./gradlew :app:connectedDebugAndroidTest` (tablet connected).
6. **Worklog:** when the owner confirms the stage works, add the stage entry to `WORKLOG.md` §2 (template in the file), update §1.9 (actual time and status), append to §3 Decisions, §4 Issues and the §9 screenshot index, and end the entry with the stage's **Gen AI Reflection** table (from the plan, adjusted if the work changed).
7. **Commit commands:** give exactly this, with the plan's commit message:
   ```bash
   git add -A
   git status --short        # check: no CLAUDE.md, WORKLOG.md, screenshots/ or .claude/ listed
   git commit -m "<commit message>"
   git push
   ```
   (The very first push is `git push -u origin main`.)
8. Wait for the owner to say it's pushed, then go to the next substage.

**Fix commits:** if testing reveals a bug, fix it as a separate small commit (e.g. `Fix booking dialog not surviving rotation`) after the substage's commit. These are welcome: they're realistic and the owner wants 60–80 commits in total.

## Building on this Mac
- If `./gradlew` can't find Java, use Android Studio's bundled JDK:
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
- `local.properties` (SDK path) is created by Android Studio on first sync. If it's missing, ask the owner to open the project in Android Studio and sync once, or write `sdk.dir=/Users/<name>/Library/Android/sdk`. It's git-ignored.
- 8 GB RAM: avoid running Gradle from the terminal while Android Studio is also building. If the Mac slows down, run `./gradlew --stop`. The Gradle heap is already capped at 2 GB.
- Room writes schema files to `app/schemas/`; they're part of the commit when they appear (05.3 and 18.1).

## Writing the worklog (`WORKLOG.md`)
- It's the owner's **exemplar report**, read by students. Pitch it to **a fellow student**: assume they can install Android Studio and run an app, and explain decisions, not button clicks.
- Per stage it must cover: dates and time (planned vs actual: ask the owner for actual time or estimate from the session), what was built, key decisions with reasons, issues with the solutions considered and used, testing done with results, and screenshots.
- **Screenshots:** insert placeholders exactly like `> 📸 **S07**: Database Inspector showing the seeded cats table.` and tell the owner the file name to save (`screenshots/S07.png`). Use the IDs from the plan, in order. If an extra screenshot is genuinely useful, add the next free ID. Don't overdo it.
- **Gen AI Reflection** at the end of every stage: a table of *demo* prompts a student could ask about specific files in this project, with the type of assistance. They are illustrative, not a log of real prompts; the disclaimer in §8 covers this. Then add one line on how the output should be verified.
- Concise, specific, honest. Use tables where they help. No filler.

## Project conventions (match them in any fix)
- MVVM + unidirectional data flow: `UiState` data class, sealed `Event` interface, `StateFlow`, stateless `…Content` composables.
- Business rules in `domain/` (pure Kotlin, unit-tested); multi-step DB changes in `withTransaction`; every credit change through `recordCredits`.
- KDoc on public APIs, short "why" comments, reusable composables in `ui/components`.
- Brand: dark-first, red / gray / black, premium, with cute cat touches. Must keep: MainActivity → Cat Detail / Basket → BookingActivity / CheckoutActivity (Intent + Parcelable) → result back (Activity Result API).
