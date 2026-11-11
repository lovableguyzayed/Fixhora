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
- [x] Previews batch 1 (Login) built: `mockups/index.html` — **waiting for owner's OK**
- [ ] Previews batch 2 (Account screen, helper home without fake numbers, customer "My tasks") — not started
- [x] Phase 3, round 1 — non-visual fixes + small visual bug fixes (commits 710c3fa…936a7c6)
- [ ] Phase 3, round 2 — blocked on decisions D1–D14 and preview approval
- [x] Phase 4 for round 1 — build/tests/lint/16KB/PDF script re-run; `MANUAL_TEST_CHECKLIST.md` written

## Phase 3 fix log (one commit each)
See the Status column of the issue table in the report. Updated after each commit.

## Still open (no decision needed, not done yet)
- UI-07 small text sizes / design-token pass · UI-10 tablet max width
- I18N-01 move all text to `strings.xml` + Hindi (needs D13 for wording)
- Dead buttons on helper screens that need new screens (notifications, filters, "Explore nearby jobs")
- SEC-06 backup rules (when login tokens exist)
- Mobile field: show "+91" even before the field is tapped (part of batch 1 mockup)
