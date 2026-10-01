# Handover: rebuilding The Feline Co. in your new repo

This folder lets you rebuild the finished app **stage by stage** in a fresh repository. Claude Code works
locally next to you: it applies one prepared change at a time, builds it, tests it with you, writes up
the worklog, and gives you the git commands. **You** run every commit and push yourself.

| File | What it's for | Goes where |
|---|---|---|
| `patches/` | 65 ordered changes (01.1 … 19.2), each checked to compile with unit tests passing | stays here |
| `BUILD_PLAN.md` | The stage-by-stage script: patch, commit message, what to test, screenshots, worklog notes, demo AI prompts | stays here |
| `CLAUDE.md` | Rules and workflow for the local Claude (never commits; gives you commands) | **copy** into the new repo, local-only |
| `WORKLOG.md` | Your report. Planning & Research is already written; Claude fills in each stage | **copy** into the new repo, local-only |
| `00.1-README.md` | README text for the first manual step | used by step 00.1 |
| `claude-settings.local.json` | Blocks `git commit` / `git push` for Claude | **copy** to `.claude/settings.local.json`, local-only |

## One-time setup (macOS Terminal)

**1. Put my work next to your new repo** (replace `YOUR-NEW-REPO` with your repo's folder name):

```bash
cd ~/AndroidStudioProjects
git clone --branch claude/dreamy-hypatia-8zfjtr --single-branch https://github.com/anasalazad/Project-2-.git feline-source
```

*No access to that repo from this account?* On GitHub, open `anasalazad/Project-2-`, switch the branch
dropdown to `claude/dreamy-hypatia-8zfjtr`, then **Code → Download ZIP**. Unzip it, rename the folder to
`feline-source` and move it into `~/AndroidStudioProjects/`.

You should now have:

```
~/AndroidStudioProjects/
├── feline-source/        ← my work: the reference app + this handover folder (don't edit)
└── YOUR-NEW-REPO/        ← the repo you cloned in Android Studio
```

**2. Add the local-only files to your new repo and hide them from git:**

```bash
cd ~/AndroidStudioProjects/YOUR-NEW-REPO
cp ../feline-source/handover/CLAUDE.md .
cp ../feline-source/handover/WORKLOG.md .
mkdir -p screenshots .claude
cp ../feline-source/handover/claude-settings.local.json .claude/settings.local.json
printf '\n# Local-only (never commit)\nCLAUDE.md\nWORKLOG.md\nscreenshots/\n.claude/\n.idea/\n.DS_Store\n' >> .git/info/exclude
git status --short
```

The last command should print **nothing**: the new files are invisible to git. `.git/info/exclude`
works like `.gitignore` but is never committed, so nobody sees these files on GitHub.

**3. Make sure commits carry your name** (skip if already set):

```bash
git config user.name "Your Name"
git config user.email "you@example.com"
```

**4. Start Claude Code in the new repo** and give it this first message:

```bash
cd ~/AndroidStudioProjects/YOUR-NEW-REPO
claude
```

> Read CLAUDE.md. We're rebuilding The Feline Co. from the patches in ../feline-source/handover.
> Start with Stage 0 (step 00.1), then continue substage by substage. Never commit or push. Give me the commands.

If Claude says it can't read `../feline-source`, type `/add-dir ../feline-source` in Claude Code.

## What a normal cycle looks like
1. Claude applies the next patch and builds it.
2. At the end of each stage, you run the app on the tablet, tick the checklist and take the listed screenshots (`S01.png`, `S02.png` … into `screenshots/`).
3. Claude updates `WORKLOG.md`.
4. Claude gives you `git add -A` / `git commit -m "…"` / `git push`, and you run them.

65 planned commits + optional photo/font commits + any fix commits ≈ 70–80 in total.

## Good to know
- **Android Studio + Gradle on 8 GB:** let one thing build at a time. Close the emulator if you're using the tablet.
- **AGP/Kotlin upgrade prompts:** choose *Don't upgrade*. Versions are pinned and compatible.
- **Photos:** optional, any time after stage 4 (`ASSETS.md` in `feline-source` lists the names). Placeholders show until then.
- **Already-written code:** `feline-source/app` is the finished app, if you or Claude want to look ahead.
