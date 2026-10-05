#!/usr/bin/env python3
"""Check the string resources against the code that uses them.

Run before pushing anything that touches `res/values*/strings.xml` or a `stringResource` call.
Every check here corresponds to a way a build has actually broken or a translation has actually
gone wrong on this project:

  duplicate     a name defined twice in one file. aapt2 fails the build with
                "Found item String/x more than one time". Set-based checks cannot see this,
                which is exactly how it reached CI once.
  missing       code references a name no locale defines -> unresolved reference at compile time.
  locale-only   a name defined in values-hi but not values -> the default locale has no fallback.
  unused        defined and referenced by nothing. Not a build failure; it is dead weight and
                usually means a rename left a stray behind.
  arity         the number of format arguments at the call site differs from the resource ->
                IllegalFormatException at runtime, on whichever screen happens to use it.
  placeholders  a translation whose placeholders differ from the default locale. Hindi reverses
                argument order often enough that this is a live risk, and it also fails at
                runtime rather than at build time.

Untranslated names are reported for information only: Android falls back to the default locale,
which is the intended behaviour for brand words and for each language's own name.
"""
import collections
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

RES = pathlib.Path("app/src/main/res")
SRC = pathlib.Path("app/src")
DEFAULT = RES / "values" / "strings.xml"

# Names whose Hindi is deliberately the English: the product name, each language's own label
# (shown in its own script so a reader who cannot read the current language can still find
# theirs), and a debug-only control.
INTENTIONALLY_UNTRANSLATED = {
    "brand_fixora",
    "brand_x",
    "language_english",
    "language_hindi",
    "photos_debug_sample",
}

PLACEHOLDER = re.compile(r"%\d*\$?[sd]")


def entries(path):
    """{name: text} per tag, plus the duplicate names, from one strings.xml."""
    root = ET.parse(path).getroot()
    out, dups = {}, {}
    for tag in ("string", "plurals"):
        names = [e.get("name") for e in root.findall(tag)]
        repeated = {n: c for n, c in collections.Counter(names).items() if c > 1}
        if repeated:
            dups[tag] = repeated
        for e in root.findall(tag):
            out[(tag, e.get("name"))] = "".join(e.itertext())
    return out, dups


def referenced():
    """{(tag, name)} referenced from Kotlin, and the arg count at each stringResource call."""
    names, arities = set(), collections.defaultdict(set)
    call = re.compile(r"stringResource\(\s*R\.string\.(\w+)((?:[^()]|\([^()]*\))*)\)")
    for f in SRC.rglob("*.kt"):
        text = f.read_text()
        for n in re.findall(r"R\.string\.(\w+)", text):
            names.add(("string", n))
        for n in re.findall(r"R\.plurals\.(\w+)", text):
            names.add(("plurals", n))
        for m in call.finditer(text):
            args = [a for a in m.group(2).split(",") if a.strip()]
            arities[m.group(1)].add(len(args))
    return names, arities


def declared_arity(text):
    """How many format arguments a resource string takes."""
    positional = [int(p) for p in re.findall(r"%(\d+)\$", text)]
    return max(positional) if positional else len(re.findall(r"%[sd]", text))


def main():
    problems = []
    locales = sorted(RES.glob("values*/strings.xml"))
    parsed = {}
    for path in locales:
        try:
            parsed[path], dups = entries(path)
        except ET.ParseError as exc:
            problems.append(f"{path}: not well-formed XML: {exc}")
            continue
        for tag, repeated in dups.items():
            for name, count in sorted(repeated.items()):
                problems.append(f"{path}: <{tag} name=\"{name}\"> defined {count} times")

    if DEFAULT not in parsed:
        print("\n".join(problems) or f"{DEFAULT} could not be read", file=sys.stderr)
        return 1

    base = parsed[DEFAULT]
    used, arities = referenced()

    for tag, name in sorted(used - set(base)):
        problems.append(f"code references R.{tag}.{name}, which values/strings.xml does not define")
    for tag, name in sorted(set(base) - used):
        problems.append(f"values/strings.xml defines {tag}/{name}, which nothing references")

    for name, counts in sorted(arities.items()):
        want = declared_arity(base.get(("string", name), ""))
        for got in sorted(counts):
            if ("string", name) in base and got != want:
                problems.append(
                    f"R.string.{name} takes {want} format argument(s) but is called with {got}"
                )

    for path, items in parsed.items():
        if path == DEFAULT:
            continue
        for key in sorted(set(items) - set(base)):
            problems.append(f"{path}: defines {key[0]}/{key[1]}, absent from values/strings.xml")
        for key in sorted(set(items) & set(base)):
            here = sorted(PLACEHOLDER.findall(items[key]))
            there = sorted(PLACEHOLDER.findall(base[key]))
            if here != there:
                problems.append(
                    f"{path}: {key[0]}/{key[1]} has placeholders {here}, default has {there}"
                )

    if problems:
        print(f"{len(problems)} problem(s):\n")
        print("\n".join(problems))
        return 1

    print(f"OK - {len(base)} resources, {len(locales)} locale(s)")
    for path, items in sorted(parsed.items()):
        if path == DEFAULT:
            continue
        missing = sorted(n for _, n in set(base) - set(items))
        unexpected = [n for n in missing if n not in INTENTIONALLY_UNTRANSLATED]
        note = f", {len(unexpected)} unexpected" if unexpected else ""
        print(f"     {path.parent.name}: {len(items)} translated, {len(missing)} fall back{note}")
        for name in unexpected:
            print(f"       untranslated: {name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
