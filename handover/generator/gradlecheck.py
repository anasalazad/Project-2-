import re, subprocess, sys
D = "staged"
tags = subprocess.run("git tag --sort=creatordate", cwd=D, shell=True, capture_output=True, text=True).stdout.split()
tags = sorted(tags)
bad = 0
for t in tags:
    def show(p):
        r = subprocess.run(f"git show {t}:{p}", cwd=D, shell=True, capture_output=True, text=True)
        return r.stdout if r.returncode == 0 else None
    cat = show("gradle/libs.versions.toml")
    sec, libs, plugins, versions = None, set(), set(), set()
    for line in cat.splitlines():
        m = re.match(r"\[(\w+)\]", line)
        if m: sec = m.group(1); continue
        m = re.match(r"([\w-]+)\s*=", line)
        if not m: continue
        key = m.group(1)
        if sec == "libraries": libs.add(key.replace("-", "."))
        elif sec == "plugins": plugins.add(key.replace("-", "."))
        elif sec == "versions": versions.add(key)
        refs = re.findall(r'version\.ref = "(\w+)"', line)
        for r_ in refs:
            if r_ not in [v for v in versions] and sec != "versions":
                pass
    # version refs
    for line in cat.splitlines():
        for r_ in re.findall(r'version\.ref = "(\w+)"', line):
            if r_ not in versions: print(t, "missing version", r_); bad += 1
    for f in ["build.gradle.kts", "app/build.gradle.kts"]:
        s = show(f)
        if s is None: continue
        for a in re.findall(r"libs\.plugins\.([\w.]+)", s):
            if a not in plugins: print(t, f, "missing plugin", a); bad += 1
        for a in re.findall(r"libs\.(?!plugins\.)([\w.]+)", s):
            if a not in libs: print(t, f, "missing lib", a); bad += 1
        # plugins used in app must be declared in root
    app = show("app/build.gradle.kts") or ""
    root = show("build.gradle.kts")
    for a in re.findall(r"alias\(libs\.plugins\.([\w.]+)\)\n", app):
        if f"libs.plugins.{a})" not in root: print(t, "plugin not in root", a); bad += 1
    if "ksp(" in app and "ksp {" not in app: print(t, "ksp block missing"); bad += 1
print("tags:", len(tags), "problems:", bad)
