#!/usr/bin/env python3
"""Flag stringResource / pluralStringResource calls that sit in a non-@Composable function.

Scope is determined by indentation, which holds for this codebase because every file is
ktfmt-formatted. A call inside a lambda argument is attributed to the nearest enclosing `fun`,
which is the conservative reading: if that fun is composable the call is legal, and if it is not,
the lambda is a plain lambda and the call really is illegal.
"""
import re
import sys
import pathlib

FUN = re.compile(r"^(\s*)(?:@\w+\s+)*(?:public |private |internal |protected )?(?:inline |suspend |operator |infix |override |tailrec )*fun\b")
CALL = re.compile(r"\b(?:plural)?[sS]tringResource\s*\(")

def indent(line):
    return len(line) - len(line.lstrip())

problems = []
for path in sorted(pathlib.Path("app/src/main/java").rglob("*.kt")):
    lines = path.read_text().splitlines()
    # For each fun declaration: (start, end_exclusive, is_composable, name)
    funs = []
    for i, line in enumerate(lines):
        m = FUN.match(line)
        if not m:
            continue
        n = len(m.group(1))
        # Walk up past annotations / comments to see whether @Composable is attached.
        composable = "@Composable" in line
        j = i - 1
        while j >= 0 and not composable:
            s = lines[j].strip()
            if s.startswith("@"):
                if s.startswith("@Composable"):
                    composable = True
                j -= 1
            elif s.startswith("*") or s.startswith("/*") or s.startswith("//") or s.endswith("*/") or s == "":
                j -= 1
            else:
                break
        # Walk past the signature before looking for the end of the body: a parameter list may span
        # many lines, and its closing `) {` sits at the fun's own indent, which would otherwise look
        # like the end of the body. The signature itself stays inside the fun's range, so that a
        # @Composable default argument (`text: String = stringResource(...)`) is still attributed to
        # the function it belongs to — legal on a composable, an error on a plain one.
        depth = 0
        body_from = i + 1
        for k in range(i, len(lines)):
            depth += lines[k].count("(") - lines[k].count(")")
            if depth <= 0 and ("(" in lines[k] or k > i):
                body_from = k + 1
                break
        end = len(lines)
        for k in range(body_from, len(lines)):
            s = lines[k].strip()
            if not s:
                continue
            if indent(lines[k]) <= n:
                end = k + 1 if s.startswith("}") else k
                break
        name = line.strip()
        funs.append((i, end, composable, name))

    for i, line in enumerate(lines):
        if not CALL.search(line):
            continue
        if line.strip().startswith("*") or line.strip().startswith("//"):
            continue
        enclosing = [f for f in funs if f[0] <= i < f[1]]
        if not enclosing:
            problems.append(f"{path}:{i+1}: call outside any fun -> {line.strip()}")
            continue
        # Innermost = the one that starts latest.
        start, end, composable, name = max(enclosing, key=lambda f: f[0])
        if not composable:
            problems.append(f"{path}:{i+1}: inside non-composable `{name}` (line {start+1})\n    {line.strip()}")

if problems:
    print(f"{len(problems)} problem(s):\n")
    print("\n".join(problems))
    sys.exit(1)
print("OK - every stringResource call sits in a @Composable function")
