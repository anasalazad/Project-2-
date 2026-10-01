import json, importlib.util
spec = importlib.util.spec_from_file_location("pc", "plan_content.py"); pc = importlib.util.module_from_spec(spec); spec.loader.exec_module(pc)
meta = json.load(open("patch-meta.json"))
PATCH_DIR = "../feline-source/handover/patches"
by_stage = {}
for m in meta:
    by_stage.setdefault(m["id"].split(".")[0], []).append(m)

def files_md(files):
    out = []
    for f in files:
        parts = f.split("\t")
        code = parts[0][0]
        label = {"A": "add", "M": "edit", "D": "delete", "R": "rename"}.get(code, code)
        path = parts[-1]
        out.append(f"`{label}` {path.replace('app/src/main/java/com/thefelineco/', '…/').replace('app/src/test/java/com/thefelineco/', 'test/…/').replace('app/src/androidTest/java/com/thefelineco/', 'androidTest/…/')}")
    return out

L = []
w = L.append
total = len(meta) + 1
w("# BUILD PLAN: The Feline Co. (local, stage-by-stage rebuild)")
w("")
w("> **Local-only file.** Never commit it. Read `CLAUDE.md` first: it explains the loop you run for every substage.")
w("")
w(f"The finished app is split into **{len(meta)} patches** (plus one manual step, 00.1), grouped into 19 stages.")
w("Every patch has been verified to compile with its unit tests passing, so after each substage the app should build. **Each stage ends with a runnable app**")
w("that the owner tests on the tablet before moving on. Apply patches **in order**; never skip one.")
w("")
w("Patch command (run from the new repo root):")
w("")
w("```bash")
w(f"git apply --whitespace=nowarn {PATCH_DIR}/<patch-file>")
w("```")
w("")
w("## Overview")
w("")
w("| Stage | Title | Substages | Screenshots |")
w("|---|---|---|---|")
w("| 0 | Repository setup | 00.1 | — |")
for sid, info in pc.STAGES.items():
    subs = by_stage[sid]
    shots = ", ".join(s for s, _ in info["shots"])
    w(f"| {int(sid)} | {info['title']} | {subs[0]['id']}–{subs[-1]['id']} ({len(subs)}) | {shots} |")
w("")
w("---")
w("")
w("## Stage 0: Repository setup")
w("")
w("### 00.1 Rename README and describe the project *(manual, no patch)*")
w("The new repo has `README.MD` (upper-case extension). Rename it and replace its contents with the intro file:")
w("")
w("```bash")
w("git mv README.MD README.md     # if git says the destination exists, use: git mv -f README.MD README.md")
w("cp ../feline-source/handover/00.1-README.md README.md")
w("```")
w("")
w("**Commit message:** `Rename README and describe the project`")
w("")
w("**Worklog:** Section 1 (Planning & Research) is already written. Ask the owner to read it, fill in their name/ID/repo link,")
w("and adjust anything they disagree with. Record Stage 0 time in the §1.9 table.")
w("")
for sid, info in pc.STAGES.items():
    w("---")
    w("")
    w(f"## Stage {int(sid)}: {info['title']}")
    w("")
    w(f"**Goal.** {info['goal']}")
    w("")
    for m in by_stage[sid]:
        w(f"### {m['id']} {m['msg']}")
        w(f"- **Patch:** `{m['patch']}`")
        w(f"- **Commit message:** `{m['msg']}`")
        w("- **Files:** " + " · ".join(files_md(m["files"])))
        if m["id"] in pc.SUB_NOTES:
            w(f"- **Note:** {pc.SUB_NOTES[m['id']]}")
        w("")
        if m["id"] == "04.4":
            w("### 04.5 (optional, owner) Add photos and fonts")
            w("- The owner finds the images listed in `ASSETS.md` and drops them in `app/src/main/res/drawable-nodpi/`")
            w("  (fonts in `app/src/main/res/font/`). Names must match exactly (lowercase, underscores).")
            w("- Build, glance at the previews, then commit in up to three commits:")
            w("  `Add cat photography` · `Add banner and product photos` · `Add brand fonts`.")
            w("- Record image sources (photographer + link) in the worklog References for the licence credit.")
            w("")
    w("#### Test this stage")
    for t in info["test"]:
        w(f"- [ ] {t}")
    w("")
    w("#### Screenshots")
    for s, d in info["shots"]:
        w(f"- **{s}**: {d}")
    w("")
    w("#### Worklog notes for this stage")
    w("Key design decisions (decision → why; mention an alternative where it helps):")
    for d in info["decisions"]:
        w(f"- {d}")
    if info["issues"]:
        w("")
        w("Issues to log (these really came up while this code was written; the fix is already in the patch):")
        for i in info["issues"]:
            w(f"- {i}")
    w("")
    w("Plus **any real problem hit while building or testing locally**: log it too, with the fix.")
    w("")
    w(f"#### Gen AI Reflection: Stage {int(sid)} (demo prompts for the worklog)")
    w("| # | File | Example prompt a student could ask | Type of assistance |")
    w("|---|---|---|---|")
    for n, (f, q, t) in enumerate(info["prompts"], 1):
        w(f"| {n} | `{f}` | \"{q}\" | {t} |")
    w("")
w("---")
w("")
w("## After stage 19")
w("- Worklog: complete §5 Testing Summary, §6 Reflection, the time totals in §1.9, the References (image credits) and the §9 screenshot index.")
w("- Tell the owner the plan is complete. Further changes follow the same loop: small change → build → test → worklog → commit commands.")
open("BUILD_PLAN.md", "w").write("\n".join(L) + "\n")
print(len(L), "lines")
