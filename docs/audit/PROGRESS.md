# Audit progress checklist

Branch: `claude/new-session-b1i7yv` (the session's assigned branch — used instead of `audit-fixes`).
Full report: [APP_AUDIT_REPORT.md](APP_AUDIT_REPORT.md)

## How to build here
- Android SDK at `/opt/android-sdk` (platforms 36 + 36.1, build-tools 36.1). `local.properties` has `sdk.dir` (git-ignored).
- `./gradlew assembleDebug testDebugUnitTest lintDebug --no-configuration-cache`
- Maven Central sometimes returns HTTP 429 — just re-run; Gradle keeps what it downloaded.

## Phases
- [x] Phase 0 — Discovery (in report §2)
- [x] Phase 1 — Audit (report §3)
- [x] Phase 2 — Report written
- [ ] Previews batch 1 (Login) — waiting for owner's OK
- [ ] Phase 3 — Fixes (non-visual + small visual bug fixes in progress)
- [ ] Phase 4 — Verify & hand over

## Phase 3 fix log (one commit each)
See the Status column of the issue table in the report. Updated after each commit.
