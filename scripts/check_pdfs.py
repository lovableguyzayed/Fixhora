#!/usr/bin/env python3
"""Check every PDF the app references or ships.

Scans source, resources, assets and config for ".pdf" references, then checks:
  - referenced local files exist (case-sensitive) and are valid PDFs (%PDF header,
    not empty, has at least one /Page, ends with %%EOF), flags files > 10 MB
  - referenced URLs return HTTP 200 and PDF bytes (skip with --offline)
  - orphan PDFs: present in the project but never referenced
Exit code 1 if anything is broken or missing.

Usage: python3 scripts/check_pdfs.py [--offline]
"""
import os
import re
import sys
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCAN_EXT = {".kt", ".java", ".xml", ".json", ".kts", ".properties", ".gradle", ".txt", ".csv"}
SKIP_DIRS = {".git", "build", ".gradle", "node_modules", "docs", "scripts", ".idea"}
REF = re.compile(r"""["'(]([^"'()\s]*\.pdf)(?:\?[^"'()\s]*)?["')]""", re.IGNORECASE)
MAX_BYTES = 10 * 1024 * 1024


def walk():
    for d, dirs, files in os.walk(ROOT):
        dirs[:] = [x for x in dirs if x not in SKIP_DIRS]
        for f in files:
            yield os.path.join(d, f)


def check_bytes(data):
    problems = []
    if not data:
        return ["0 bytes"]
    if not data.startswith(b"%PDF"):
        problems.append("not a PDF (no %PDF header)")
    if b"%%EOF" not in data[-2048:]:
        problems.append("truncated (no %%EOF)")
    if not re.search(rb"/Type\s*/Page[^s]", data):
        problems.append("no pages found")
    if len(data) > MAX_BYTES:
        problems.append(f"large: {len(data) / 1e6:.1f} MB")
    return problems


def main():
    offline = "--offline" in sys.argv
    refs = {}  # ref -> list of "file:line"
    present = []
    for path in walk():
        rel = os.path.relpath(path, ROOT)
        if path.lower().endswith(".pdf"):
            present.append(rel)
            continue
        if os.path.splitext(path)[1] not in SCAN_EXT:
            continue
        try:
            with open(path, encoding="utf-8", errors="ignore") as fh:
                for n, line in enumerate(fh, 1):
                    for m in REF.finditer(line):
                        refs.setdefault(m.group(1), []).append(f"{rel}:{n}")
        except OSError:
            pass

    bad = 0
    used = set()
    print(f"PDF references found: {len(refs)}   PDF files present: {len(present)}\n")
    for ref, where in sorted(refs.items()):
        if ref.startswith(("http://", "https://")):
            if offline:
                print(f"[URL ] {ref}  (not checked: --offline)  <- {where[0]}")
                continue
            try:
                with urllib.request.urlopen(ref, timeout=20) as r:
                    data = r.read(MAX_BYTES + 1)
                    probs = ([] if r.status == 200 else [f"HTTP {r.status}"]) + check_bytes(data)
            except Exception as e:  # noqa: BLE001
                probs = [f"download failed: {e}"]
        else:
            name = os.path.basename(ref)
            match = [p for p in present if os.path.basename(p) == name]
            ci = [p for p in present if os.path.basename(p).lower() == name.lower()]
            if not match:
                probs = ["MISSING" + (f" (case mismatch: {ci[0]})" if ci else "")]
            else:
                used.update(match)
                with open(os.path.join(ROOT, match[0]), "rb") as fh:
                    probs = check_bytes(fh.read())
        status = "OK  " if not probs else "FAIL"
        bad += bool(probs)
        print(f"[{status}] {ref}  {'; '.join(probs)}  <- {', '.join(where[:3])}")

    for p in present:
        if p not in used:
            with open(os.path.join(ROOT, p), "rb") as fh:
                probs = check_bytes(fh.read())
            print(f"[ORPH] {p}  (present but never referenced) {'; '.join(probs)}")

    print(f"\nResult: {bad} broken/missing, {len(set(present) - used)} orphan")
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
