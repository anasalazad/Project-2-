# The Feline Co. — notes for Claude sessions

> **Local rebuild in a different repo?** If you are running inside the owner's *new* repository and this
> folder is `../feline-source`, **ignore this file**. Follow that repo's own `CLAUDE.md`
> (from `handover/CLAUDE.md`): never commit or push, apply `handover/patches` in order, keep `WORKLOG.md`.

Android app (Kotlin, Jetpack Compose) for a university mobile development project: a premium,
cats-only adoption + pet-shop app with a credit system and an admin side. The owner tests it in
Android Studio on a **Samsung tablet** and reports issues back. The written report is out of scope.

## Read first
- `CONTEXT.md`: the agreed plan, the brief → code mapping, architecture, credit rules, decisions and progress.
  Keep it updated when something changes.
- `Specifications and requirements.md`: the course brief and marking rubric.
- `TESTING.md`: automated tests and the manual script (steps A1–G3) the owner uses to report bugs.
- `ASSETS.md`: photo and font file names the owner adds by hand to `app/src/main/res/drawable-nodpi/`.

## Hard requirements (don't break these)
- The flow in the brief: **MainActivity** (Navigation Compose) → Cat Detail / Basket →
  **BookingActivity** / **CheckoutActivity** (Intent + Parcelable) → result back to MainActivity
  (Activity Result API: `BookMeetAndGreet`, `CheckoutContract`).
- Brand: red / gray / black, dark-first, premium, with cute cat touches (paw prints, crown-cat logo).

## Conventions
- MVVM + unidirectional data flow: `UiState` data class, sealed `Event` interface, `StateFlow`,
  stateless `…Content` composables. Manual DI through `di/AppContainer` and `di/AppViewModelProvider`.
- Business rules live in `domain/` (pure Kotlin, unit-tested); multi-step DB changes run in
  `withTransaction` inside the repositories; every credit change goes through `recordCredits`.
- KDoc on public APIs, short "why" comments, reusable components in `ui/components`.
- Images are looked up by name (`AssetImage`), so missing photos never break the build.
- Room uses destructive migration in development: bump `FelineDatabase.version` when the schema changes.

## Verifying changes in a cloud session (no Android SDK)
Run `tools/compile-check/run.sh` (set `GRADLE=/path/to/gradle` if `gradle` isn't on PATH).
It compiles all app Kotlin against desktop Compose with stand-ins for Android-only APIs and runs the JVM
unit tests. If you call a new Android-only API, add a faithful stand-in under `tools/compile-check/stubs/`.
It can't check Room's annotation processor, resources or the manifest; review those by hand.

## Handover
`handover/` holds the stage-by-stage rebuild kit for the owner's new repo: 65 compile-verified patches,
`BUILD_PLAN.md`, the local `CLAUDE.md`, the starter `WORKLOG.md` and setup steps (`handover/README.md`).
If app code changes here, the patches no longer match. Regenerate them, or tell the owner.

## Git
- Work branch: `claude/dreamy-hypatia-8zfjtr` (repo `anasalazad/Project-2-`). Commit per feature with clear messages.
