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
- [x] Previews batch 2 (Account, delete confirm, helper home without fake numbers, customer "My tasks") built — **waiting for owner's OK**
- [x] Round 2 dead-button / layout fixes (459f88a, d8e41e9, 8e974a5)
- [x] Phase 3, round 1 — non-visual fixes + small visual bug fixes (commits 710c3fa…936a7c6)
- [x] Owner approved batch 1 + batch 2, Firebase (D1), brand FixoraX (D4)
- [x] Round 4: Firebase login code + test mode, Room v3 (owners, offers, safe upgrade), new login screens, customer/helper tabs, Account, log out, delete account (650a057, 86e0448)
- [ ] Next: owner sets up Firebase (docs/FIREBASE_SETUP.md); then store tasks/offers/chats in Cloud Firestore with security rules
- [ ] Open decisions: D3 SMS provider later, D5 package name, D6 payments, D7 location/maps, D10 policy texts, D11 crash reporting, D13 Hindi wording, D14 guest posting
- [x] Phase 4 for round 1 — build/tests/lint/16KB/PDF script re-run; `MANUAL_TEST_CHECKLIST.md` written

## Phase 3 fix log (one commit each)
See the Status column of the issue table in the report. Updated after each commit.

## Still open (no decision needed, not done yet)
- UI-07 design-token pass (minimum 12 sp done)
- I18N-01: login/role/task + helper screens moved to strings.xml; left: text built from values, Toasts, category names, then values-hi (Hindi wording needs D13)
- Still dead (need backend or batch-2 screens): Place Bid on helper Home card, chat Call/Attach/Mic/quick actions, Map tab
- SEC-06 backup rules (when login tokens exist)
