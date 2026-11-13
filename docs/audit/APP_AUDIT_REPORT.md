# FixoraX (repo "Fixhora") — Full App Audit

Date: 10 Nov 2026 · Audited commit: `878835e` · Branch with fixes: `claude/new-session-b1i7yv`

Evidence tags: **V** = Verified (built / ran / tested here) · **C** = Code evidence · **D** = Needs real-device test

---

## 1. Executive summary

**What the app is (inferred from code — nothing was filled in the prompt):** FixoraX is a
local-services marketplace for India. A customer ("I need help") posts a task — category, location,
photos, title, budget in ₹ — and a helper/worker ("I want to help") sees nearby jobs, accepts or bids,
and chats. Android only (native Kotlin + Jetpack Compose). The `ios/` and `web/` folders contain only
icon images, not apps.

**Release-readiness verdict: NOT READY — this is a clickable prototype, not a working app yet.**
Everything is stored only on the one phone (no server). A task posted on Ramesh ji's phone can never
be seen by a helper on another phone. Login screens exist but check nothing: any number, any OTP, any
password "logs you in". There are no payments and no PDFs in the app at all.
The screens themselves are a decent base and the brand look is good — the work is to connect them to
a real backend and remove the fake parts.

### Top issues
| # | ID | Problem | Sev |
|---|----|---------|-----|
| 1 | ARCH-01 | No backend: tasks, bids and chats live only on one phone; customer and helper can't reach each other | P0 |
| 2 | AUTH-01 | Login/OTP/sign-up accept anything; no OTP is ever sent; no account exists | P0 |
| 3 | BRK-01 | App secretly inserts 100 fake US tasks ("New York, NY") into the real database | P0 |
| 4 | BRK-07 | "Use my current location" is ON by default and posts a fake "Sector 62, Noida" address | P1 |
| 5 | BRK-11/12 | Helper home shows fake earnings (₹1,250), fake rating 4.9, fake name "Alex Worker"; Map tab is not a map | P1 |
| 6 | BRK-17 | Customer can't see their own posted tasks or any offers — no "My tasks" screen | P1 |
| 7 | BRK-03 | Posting a task can recreate it as a draft; double-tap posts it twice | P1 |
| 8 | AUTH-09 | After login, Back goes to the login screens again | P1 |
| 9 | UI-00 / UI-08 | Sign-in, Mobile, OTP, Profile screens show dark text on a **black** background (unreadable); in dark mode status-bar icons vanish | P0 |
| 10 | ICON-01 | All 20 app-icon files inside the app were corrupted → blank icon, release build failed | P0 (fixed) |
| 11 | AUTH-13 / SEC-03 | No logout, profile, settings, privacy policy, terms, help or delete-account | P1 |

---

## 2. Phase 0 — Discovery

### 2.1 Tech stack
| Item | Finding |
|---|---|
| Framework | Native Android, Kotlin 2.2.10, Jetpack Compose (BOM 2024.09), Material 3 |
| Navigation | Navigation-Compose 2.8.9, string routes; 3 NavHosts (root `FixhoraApp.kt`, task flow, helper tabs) |
| State | ViewModels + StateFlow (`TaskViewModel`, `HelperViewModel`); screen state via `remember` |
| Storage | Room 2.7 (`tasks`, `chat_messages`), `fallbackToDestructiveMigration()` |
| Backend | **None.** Retrofit/OkHttp/Moshi are in Gradle but never used |
| Auth provider | **None** (UI only) |
| Payment SDK | **None** |
| PDF libraries | **None** |
| Images | Coil 2.7 |
| SDK levels | `minSdk 24` (Android 7.0), `targetSdk 36`, `compileSdk 36.1` — meets Play's current API 36 rule ✓ |
| Build | AGP 9.1.1; no Gradle wrapper was committed (added in this audit, Gradle 9.3.1) |
| Origin | Generated with Google AI Studio (`metadata.json`, README) |
| App name | "FixoraX" in the app; "Fixhora" in repo, design file, DB name; "My Application" in Gradle — see decision D4 |
| Package | `applicationId = com.aistudio.fixhora.xyzkpa`, `namespace = com.example` — see decision D5 |

### 2.2 Screen inventory
| Screen / route | Purpose | File | Status |
|---|---|---|---|
| `splash` | Logo + tagline, 2.5 s wait | `auth/SplashAndWelcome.kt:30` | Working (artificial delay) |
| `welcome` | Sign In / Create Account / Guest | `auth/SplashAndWelcome.kt:89` | Partly working (no real auth behind it) |
| `sign_in` | Mobile + password login, OTP/Google/Email buttons | `auth/SignInAndSignUp.kt:30` | Placeholder (accepts anything) |
| `create_account` | Name, mobile, email, password ×2 | `auth/SignInAndSignUp.kt:179` | Placeholder |
| `forgot_password` | 3-step reset | `auth/MobileLoginFlow.kt:200` | Broken (OTP/password fields can't be typed in) |
| `mobile_login` | +91 + phone number | `auth/MobileLoginFlow.kt:27` | Placeholder (no SMS sent) |
| `otp_verification` | 6 OTP boxes, resend timer | `auth/MobileLoginFlow.kt:92` | Placeholder (any/empty OTP passes) |
| `profile_setup` | Name, gender, DOB, city, state, PIN, language | `auth/ProfileAndPermissions.kt:28` | Placeholder (nothing saved) |
| `permission_request` | Location + notification explainer | `auth/ProfileAndPermissions.kt:150` | Broken (asks for no permission) |
| `role_selection` | "I need help" / "I want to help", EN/हिंदी | `RoleSelectionScreen.kt:49` | Working (language not saved) |
| `task_flow/category` | Pick category, search | `taskflow/TaskCategoryScreen.kt:32` | Working |
| `task_flow/location` | Address search, current-location switch | `taskflow/TaskLocationScreen.kt:31` | Partly working (fake GPS/suggestions) |
| `task_flow/photos` | Photos + title + description + budget | `taskflow/TaskPhotosScreen.kt:39` | Partly working |
| `task_flow/review` | Review + Post | `taskflow/TaskReviewScreen.kt:32` | Working (local only) |
| `task_flow/success` | "Task posted" | `TaskFlowContainer.kt:149` | Working |
| `helper_home` (tab) | Stats + nearby jobs feed | `helper/WorkerHomeScreen.kt:30` | Partly working (fake stats) |
| `helper_map` (tab) | Map of jobs | `helper/WorkerMapScreen.kt:29` | Placeholder (grey box) |
| Job details sheet | Bottom sheet | `helper/WorkerMapScreen.kt:184` | Partly working |
| Place-bid sheet | Bottom sheet | `helper/WorkerMapScreen.kt:202` | Broken (bid = accept, amount discarded) |
| `helper_chat` (tab) | Chat list + conversation | `helper/WorkerChatScreen.kt:32` | Partly working (worker-only, fake customer msg) |
| `helper_tasks` (tab) | Task list with filters | `helper/WorkerTasksScreen.kt:30` | Partly working |
| *(missing)* Customer "My tasks", offers, profile, settings, help, policies | — | — | Missing |

### 2.3 Action inventory (everything that does nothing or the wrong thing)
| Where | Control | Should | Actually |
|---|---|---|---|
| Sign In | Sign In | Verify credentials | Goes straight in (`SignInAndSignUp.kt:118`) |
| Sign In | Keep me signed in | Persist session | Nothing (`:78`) |
| Sign In / Create | Continue / Sign Up with Google | Google sign-in | "Coming soon" toast (`:142,:288`) |
| Sign In | Continue with Email | Email login | "Coming soon" toast (`:144`) |
| Create Account | Create Account | Validate + create | Goes straight in, terms checkbox ignored (`:264`) |
| Mobile login | Send OTP | Send SMS | Navigates only (`MobileLoginFlow.kt:78`) |
| OTP | Verify | Check code | Always passes, even empty (`:176`) |
| OTP | Call Me Instead | Voice OTP | Nothing (`:170`) |
| Forgot password | OTP / new password fields | Typing | Can't type (`:256,:282,:290`) |
| Profile setup | Photo circle | Pick photo | Not clickable (`ProfileAndPermissions.kt:64`) |
| Profile setup | Continue | Save profile | Saves nothing (`:121`) |
| Permissions | Allow Permissions | Ask Android | Asks nothing (`:212`) |
| Role selection | हिंदी | Switch app language | Only this screen; forgotten after (`RoleSelectionScreen.kt:50`) |
| Task location | ⌖ icon in search field | Locate me | Nothing (`TaskLocationScreen.kt:68`) |
| Task location | "Within 5 km" card (has ›) | Change radius | Not clickable (`:224`) |
| Task location | "Location is safe" card (has ›) | Explain | Not clickable (`:250`) |
| Task photos | "⚡ Add sample photos for testing" | (developer tool) | Visible to real users (`TaskPhotosScreen.kt:179`) |
| Helper home | Bell (badge "2") | Notifications | Nothing (`WorkerHomeScreen.kt:48`) |
| Helper home | Online/Offline | Go online | Not saved/sent (`:36`) |
| Helper home | Place Bid | Bid sheet | Nothing (`:137`) |
| Helper home | Refresh (empty state) | Reload | Nothing (`:124`) |
| Map | Filter, Refresh, My-location FABs, mic | — | Nothing (`WorkerMapScreen.kt:51,65,74,144`) |
| Map | Search + category chips | Filter jobs | Chips/search don't filter |
| Map sheet | Chat with Customer | Open chat | Nothing (`:196`) |
| Bid sheet | Submit Bid | Send bid | Accepts the job instead, bid lost (`:210`) |
| Tasks tab | Search / Filter / Sort icons, search box | — | Nothing (`WorkerTasksScreen.kt:46-54,62`) |
| Tasks tab | "In Progress", "Cancelled" chips | Filter | Always empty (`:88`) |
| Tasks tab | Chat, Navigate, View Details, Place Bid, card tap | — | Nothing (`:156-184`) |
| Tasks tab | "Explore Nearby Jobs" (empty) | Go to Home | Nothing (`:112`) |
| Chat | Filter, chips, search, Call, More, Attach, Mic, 4 quick actions, View Details | — | Nothing (`WorkerChatScreen.kt`) |
| Chat list | "Explore Nearby Jobs" | Go to Home | Nothing (`:89`) |
| Helper nav | Chat badge "3" | Unread count | Hardcoded (`HelperFlowContainer.kt:53`) |

### 2.4 PDF inventory
**The app has no PDFs** — none bundled, downloaded or generated, and no PDF library. The check script
`scripts/check_pdfs.py` (added in this audit) scans code, resources and assets for `.pdf` references
and files and validates each one; result: **0 referenced, 0 present, 0 broken** (V).
The chat's "Send Invoice"/"Send Quote" buttons imply invoices will be needed later → backlog item.

### 2.5 Config & services (locations only, no values)
| Item | Location | Note |
|---|---|---|
| Gemini key placeholder | `.env.example` (`GEMINI_API_KEY=MY_GEMINI_API_KEY`) | Read by Secrets Gradle plugin into `BuildConfig`; unused. Never put a real key in an app (C) |
| Release keystore | env `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` (`app/build.gradle.kts:22`) | Good — no secrets in repo |
| Debug keystore | `debug.keystore` (git-ignored, not present) | Standard Android debug password |
| Firebase / google-services.json | Not present | No Firebase project connected |
| OAuth client IDs / payment keys | None | — |
| Environments | Only debug/release build types; no dev/staging/prod server config | — |
| Git-history secret scan | Clean (V) | |

### 2.6 What can run here
- ✅ Build debug/release, unit tests, Android Lint (Android SDK installed in this session) (V)
- ✅ Real screenshots of Compose screens on the computer via Robolectric + Roborazzi (already in the project)
- ✅ HTML mockups rendered to PNG with Playwright
- ❌ No emulator (no hardware virtualisation) → OTP, camera/photo picker, GPS, back gesture, OEM behaviour = **D**

---

## 3. Issue table

Status: **Open** · **Fixed** (commit on branch) · **Preview** (mockup waiting for your OK) · **Decision** (needs you)

### A1. Sign up / sign in
| ID | Screen / file:line | Problem | Persona | Sev | Fix | Effort | Status |
|---|---|---|---|---|---|---|---|
| AUTH-01 | `SignInAndSignUp.kt:118,264`, `MobileLoginFlow.kt:78,176` | No real auth: any number/OTP/password works; no SMS sent; no account stored (C) | All | P0 | Needs backend + OTP provider (D1–D3) | L | Decision |
| AUTH-02 | `SplashAndWelcome.kt:84-108`, `SignInAndSignUp.kt` | Confusing: separate Sign In vs Create Account, passwords, 5 login buttons. Indian users expect one "Continue with mobile number" + "Continue with Google" | Ramesh, Priya | P1 | Single entry screen, new/returning detected automatically; drop passwords | M | Preview (batch 1) |
| AUTH-03 | `SignInAndSignUp.kt:142,144,288` | Google & Email buttons only show "Coming soon" | Priya, Amit | P1 | Credential Manager "Sign in with Google" once backend chosen | M | Decision |
| AUTH-04 | `MobileLoginFlow.kt:62`, `SignInAndSignUp.kt:62,213` | Phone field accepts letters, any length, "+91"/"0"/spaces not cleaned; no 6-9 check | Ramesh | P2 | Normalise to 10 digits, validate, disable Send OTP until valid | S | Fixed |
| AUTH-05 | `MobileLoginFlow.kt:130-151` | OTP: 6 separate boxes — no auto-advance, Backspace doesn't go back, paste impossible, no SMS autofill; 6×48dp overflows a 320dp phone | Ramesh, Amit | P2 | One input drawn as 6 boxes: typing, Backspace, paste, fits any width (V). Automatic SMS read (SMS Retriever) comes with the OTP provider | S | Fixed (auto-read: Preview/Decision) |
| AUTH-06 | `MobileLoginFlow.kt:94,166-171` | OTP screen doesn't show which number; "Call Me Instead" dead; 60 s timer, no attempt limit | All | P2 | Show "+91 98xxx", 30 s timer; hide Call-me until supported; limits need backend | S | Fixed (UI part) |
| AUTH-07 | `MobileLoginFlow.kt:176` | Verify: no loading, no wrong/expired message, can double-submit | All | P2 | Comes with real OTP backend; Verify disabled until 6 digits now | S | Fixed (partial) |
| AUTH-08 | `FixhoraApp.kt:31-44` | No session: every launch starts at Welcome; nothing remembered | All | P1 | Store session (encrypted) once backend exists | M | Decision |
| AUTH-09 | `FixhoraApp.kt:48,62,68,81` | After login/OTP, Back returns to Sign-in/OTP/Profile screens | All | P1 | Clear auth screens from back stack on success | S | Fixed |
| AUTH-10 | `MobileLoginFlow.kt:256,282,290` | Forgot-password OTP & password fields can't be typed in | — | P2 | Made typeable now; recommend deleting screen with passwords (D2) | S | Fixed (interim) |
| AUTH-11 | `ProfileAndPermissions.kt:28-147` | 7 free-text fields (gender, DOB as text, state, language); PIN without number pad; nothing saved | Ramesh | P2 | Number pad for PIN (done). Ask only Name (+ optional city) — D9 | S | Fixed (keyboard) / Preview |
| AUTH-12 | `ProfileAndPermissions.kt:150-227` | "Allow Permissions" requests nothing; app has no location/notification permission at all | All | P1 | Remove screen, ask in context (D8) | S | Decision |
| AUTH-13 | Profile/logout | No logout, profile, delete account (Play + App Store requirement) | All | P1 | Add Account screen (Profile, Language, Help, Policies, Logout, Delete) | M | Preview (batch 2) |
| AUTH-14 | `SignInAndSignUp.kt:78` | "Keep me signed in" checkbox does nothing | — | P3 | Remove with passwords (D2) | S | Decision |
| AUTH-15 | `SplashAndWelcome.kt:163`, `SignInAndSignUp.kt:233` | "Terms / Privacy Policy" are plain text, no pages exist | All | P1 | Host policy pages, link them (you supply text — D10) | S | Decision |
| AUTH-16 | `SignInAndSignUp.kt:22-24` etc. | Form input lost on screen rotation / app kill (`remember`) | Ramesh | P2 | `rememberSaveable` | S | Fixed |
| AUTH-17 | Account linking | One person = one account (OTP + Google) can't be checked — no accounts | — | — | Design into backend (unique phone/email, link on match) | M | Decision |

### A2. Payments
| ID | Screen / file:line | Problem | Persona | Sev | Fix | Effort | Status |
|---|---|---|---|---|---|---|---|
| PAY-01 | whole app | No payment gateway. Budget is only text. | — | Info | Decide model (D6) | — | Decision |
| PAY-02 | `TaskPhotosScreen.kt:241-263` | Budget accepts letters/decimals, no number pad, min > max allowed | Ramesh, Priya | P2 | Digits only, number pad, max 7 digits, error if min > max | S | Fixed |
| PAY-03 | `TaskReviewScreen.kt:473`, `WorkerHomeScreen.kt:150`, `WorkerTasksScreen.kt:126`, `WorkerChatScreen.kt:57` | ₹ shown without Indian grouping (₹125000 instead of ₹1,25,000); chat shows "₹ - ₹" when empty | All | P2 | Shared `formatRupees()` (en-IN), one `budgetLabel()` used everywhere | S | Fixed |
| PAY-04 | `WorkerHomeScreen.kt:85-95` | Fake money: "Today's Earnings ₹1,250", 142 jobs, 4.9★, 95 % | Helpers | P1 | Hide until real data (visible change) | S | Preview (batch 2) |
| PAY-05 | Policy | Real-world services → Play Billing NOT required; any Indian gateway (Razorpay/Cashfree/PhonePe PG) allowed. If you ever sell digital extras (e.g. "boost my task", helper subscription), those need Play Billing or user-choice billing | — | Info | — | — | Info |

### A3. PDFs
| ID | Problem | Sev | Status |
|---|---|---|---|
| PDF-01 | No PDFs exist; script added and run (0/0/0). Future invoices must embed a ₹/Devanagari font (e.g. Noto Sans) | Info | Fixed (script) |

### A4. Screen sizes, safe areas, fonts, dark mode
| ID | Screen / file:line | Problem | Sev | Fix | Status |
|---|---|---|---|---|---|
| UI-00 | `res/values/themes.xml:4`, auth screens without background | Window theme is Android's dark default and Sign-in / Create / Mobile / OTP / Forgot / Profile / Permission screens paint no background → navy text on black (V — see `mockups/before/sign_in_360x800_before.png`; D on real phone) | P0 | White app surface in `MainActivity` + light window theme | Fixed |
| UI-01 | All auth screens, Splash (`SplashAndWelcome.kt`, `SignInAndSignUp.kt`, `MobileLoginFlow.kt`, `ProfileAndPermissions.kt`) | App is edge-to-edge (`MainActivity.kt:13`) but these screens ignore system bars → content under status bar / 3-button nav bar (C, D) | P2 | `safeDrawingPadding()` | Fixed |
| UI-02 | `SplashAndWelcome.kt:89-170` | Welcome not scrollable; at 320×568 or max font, buttons/terms squeezed off screen | P2 | Scrollable, image height bounded | Fixed |
| UI-03 | `MobileLoginFlow.kt`, `ProfileAndPermissions.kt:150` | Mobile, OTP, Forgot, Permission screens not scrollable; keyboard hides buttons at large font | P2 | Scroll + keyboard padding | Fixed |
| UI-04 | `MobileLoginFlow.kt:58`, `SignInAndSignUp.kt:99-115`, `TaskFlowContainer.kt:228` | Max font: "+91" box wraps to 2 lines, "Forgot Password?" becomes a vertical strip, step labels break mid-word (V — `mockups/before/*_font200_before.png`) | P2 | Flexible widths, wrap rows, single-line labels | Fixed |
| UI-05 | `RoleSelectionScreen.kt:281` | Role card fixed 160 dp height → text clipped at max font size (V) | P2 | `heightIn(min = 160.dp)` | Fixed |
| UI-06 | `TaskCategoryScreen.kt:69-77` | Category tiles fixed 0.8 ratio, 10 sp text → clipped at max font; 3 columns even on tablets; on 320×568 phones the tiles were stuck in a ~60 dp strip under a fixed heading | P2 | Adaptive columns, min height; heading + search now scroll with the tiles | Fixed |
| UI-07 | many | Text 10–12 sp is too small for older users | P3 | No text below 12 sp now; category subtitles got proper line spacing. Full design-token pass still open | Partly fixed |
| UI-08 | `MainActivity.kt:13`, `res/values/themes.xml` | Phone in dark mode → status-bar icons white on white app; MIUI force-dark may invert colours; window bg not set (black flash) | P1 | Force dark status-bar icons, `forceDarkAllowed=false`, white window bg, light Material theme | Fixed |
| UI-09 | `Theme.kt:20-28` | `onSurface` = grey #6B7280; with 60 % alpha subtitles become ~2.6:1 contrast (fails 4.5:1; unreadable in sunlight) | P2 | `onSurface` = navy, `onSurfaceVariant` = grey | Fixed |
| UI-10 | all | Tablets/landscape: single column stretched edge-to-edge (1,200 dp wide buttons) | P3 | Content capped at 640 dp, centred (`ui/theme/AppFrame.kt`) (V screenshot) | Fixed |

### A5. Native feel & performance
| ID | file:line | Problem | Sev | Fix | Status |
|---|---|---|---|---|---|
| PERF-01 | `SplashAndWelcome.kt:33-39` | 2.5 s artificial splash on every launch (plus Android 12+ system splash = double splash) | P2 | Removed the 1.5 s hold, fade 400 ms | Fixed |
| PERF-02 | `WorkerHomeScreen.kt:36-39`, `WorkerMapScreen.kt:37-42` | Fake 1.5 s loading on Home & Map every visit | P2 | Show skeleton only until data really arrives | Fixed |
| PERF-03 | `res/drawable/*.png` | 3 illustrations = 5.6 MB PNG (1024×1536) shown at ≤160 dp | P2 | Resize + WebP (same look) | Fixed |
| PERF-04 | `app/build.gradle.kts:32-38,62-101` | Release: no shrinking (R8 off), unused Retrofit/OkHttp/Moshi/Firebase-BOM shipped | P2 | Unused libs off; R8 + resource shrink on → release APK **1.9 MB** (V builds; D run it once on a phone) | Fixed |
| PERF-05 | `app/build.gradle.kts:28,41` | Build fails on any machine without `debug.keystore` / release env vars | P2 | Use custom keystores only when present | Fixed |
| PERF-06 | `WorkerHomeScreen.kt:133`, `WorkerTasksScreen.kt:110` | Lists without stable keys | P3 | `key = { it.id }` | Fixed |
| PERF-07 | `WorkerChatScreen.kt:32-49` | In an open chat, system Back exits the whole helper area instead of closing the chat | P2 | `BackHandler` | Fixed |
| PERF-08 | — | No crash reporting or funnel analytics | P2 | Crashlytics or similar (D11) | Decision |
| PERF-09 | `TaskViewModel.kt:52,80,105` | `printStackTrace` in release | P4 | Log only in debug | Fixed |
| PERF-10 | `FixhoraApplication.kt:21` | Destructive migration: any app update that changes tables wipes all user data | P2 | Real migrations before launch (D12) | Decision |
| PERF-11 | — | Bad-network handling, offline banner, retries | P2 | Comes with backend | Open |

### A6. Security & compliance
| ID | Problem | Sev | Status |
|---|---|---|---|
| SEC-01 | No secrets in code or git history (V). Release key comes from env vars ✓ | — | OK |
| SEC-02 | Photos stored as temporary picker links; after restart they vanish (also privacy: link to user's gallery) | P1 | Fixed (copied into app storage) — see BRK-04 |
| SEC-03 | Privacy Policy, Terms, Refund, Contact, grievance officer — none exist (Play + DPDP) | P1 | Decision (D10) |
| SEC-04 | DPDP gaps: no consent notice listing data (name, phone, location, photos), no withdraw-consent, no data view/correct/erase, no grievance contact, no breach plan, no age check (tutoring category ⇒ minors possible) | P1 | Decision (D10) |
| SEC-05 | Delete account (in-app + web link) missing | P1 | Preview (batch 2) + needs backend |
| SEC-06 | `allowBackup=true` with empty rules: fine today, but must exclude login tokens once added | P3 | Open |
| SEC-07 | 16 KB pages: the APK has one native library (`libandroidx.graphics.path.so`, from Compose). All 4 ABIs are aligned to 16 KB (`0x4000`) and `zipalign -c -P 16` passes (V). Still test once on a 16 KB emulator (D) | — | OK |
| SEC-08 | Package name `com.aistudio.fixhora.xyzkpa` is permanent once uploaded; `namespace com.example` | P1 | Decision (D5) |
| SEC-09 | Personal Play account → closed test with 12 testers × 14 days before production | Info | — |

### A7. Broken, fake and half-built features
| ID | file:line | Problem | Sev | Fix | Status |
|---|---|---|---|---|---|
| ICON-01 | `app/src/main/res/mipmap-*/*.png` (20 files) | Launcher icons corrupted (image bytes saved as text; no image tool can open them) → blank app icon; release build fails when PNG processing is on (V) | P0 | Restored from the valid copies in `android/res` | Fixed |
| ARCH-01 | whole app | No server: tasks/bids/chat only on one phone | P0 | Backend (D1) | Decision |
| BRK-01 | `TaskRepository.kt:13-35`, `TaskViewModel.kt:32`, `HelperViewModel.kt:23` | Every time a screen opens with < 50 tasks, inserts 100 fake US tasks ("New York, NY"); two screens can do it at once → 200 | P0 | Never in release; debug-only, once, Indian sample data | Fixed |
| BRK-03 | `TaskViewModel.kt:57-108`, `TaskReviewScreen.kt:131` | Pending auto-save (0.5 s) runs after "Post Task" → posted task comes back as a draft; double-tap posts twice; screen leaves before save finishes | P1 | Cancel pending save, post once, navigate after save | Fixed |
| BRK-04 | `TaskPhotosScreen.kt:42-50` | Photo links expire → grey boxes after restart | P1 | Copy picked photos into app storage | Fixed |
| BRK-05 | `TaskPhotosScreen.kt:177-188` | Developer "add sample photos" button shown to users (loads Unsplash) | P2 | Debug builds only | Fixed |
| BRK-07 | `TaskViewModel.kt:18`, `TaskLocationScreen.kt:189`, `TaskReviewScreen.kt:83` | Current location ON by default, shows/posts fake "Sector 62, Noida" | P1 | Removed the fake GPS switch, fake map box and Noida text; address must be typed. **Visible change** made without preview because it posted wrong data. Real GPS needs D7 | Fixed (interim) |
| BRK-08 | `TaskLocationScreen.kt:68,224,250` | Locate-me icon, "Within 5 km", "safe" card look tappable but aren't | P2 | Remove fake chevrons/icon; radius picker later | Fixed |
| BRK-09 | `TaskFlowContainer.kt:34-37` | Step 1 is labelled "Task Details" but only picks a category; details are on step 3 "Photos" | P3 | Labels → Category · Location · Details · Review | Fixed |
| BRK-11 | `WorkerHomeScreen.kt` | Fake "Alex Worker", "Good Morning" at night, fake badges & stats, dead bell/bid/refresh | P1 | Remove fakes (visible) | Preview (batch 2) |
| BRK-12 | `WorkerMapScreen.kt` | "Map" is a grey box; bid = accept (bid lost); dead buttons | P1 | Real map (D7) or hide tab; bids need backend | Decision |
| BRK-13 | `WorkerTasksScreen.kt:96,137` | Rejected tasks labelled "Draft"; "Pending"/"In Progress"/"Cancelled" chips can never match; search dead | P2 | "Rejected" label + chip; never-matching chips removed; search filters by title/details/place. Sort menu (newest / highest budget) added; duplicate Search/Filter icons removed | Fixed |
| BRK-14 | `WorkerChatScreen.kt` | Fake customer message & "Online"; customers have no chat; times all "Just now" | P1 | Removed fake message/Online; real IST times. Two-way chat needs backend | Fixed (fakes) / Decision |
| BRK-15 | `HelperFlowContainer.kt:53` | Chat badge always "3" | P3 | Remove fake badge | Fixed |
| BRK-16 | `RoleSelectionScreen.kt:50` | Hindi choice not saved, not app-wide; "change later in settings" — no settings | P2 | App-wide i18n (I18N-01) | Open |
| BRK-17 | — | Customer can't see posted tasks or offers | P1 | "My tasks" screen | Preview (batch 2) |
| I18N-01 | all `.kt` | All text hardcoded in Kotlin; only role screen has Hindi | P2 | 116 texts of login, role and task-posting screens moved to `strings.xml` (screens pixel-identical). Left: ~50 texts built from values / pop-ups / role-screen Hindi pairs, all helper screens, then `values-hi` (D13) | Partly fixed |

### B. QA walkthrough highlights (personas)
| # | Persona | Steps | Expected | Actual | Sev | ID |
|---|---|---|---|---|---|---|
| Q1 | Ramesh ji | Types `+91 98765-43210` in mobile field | Cleans to 9876543210 | Kept as typed; letters allowed | P2 | AUTH-04 |
| Q2 | Ramesh ji | Reads OTP in SMS app, comes back, phone killed the app | OTP screen restored | Restarts at Splash (no saved state) | P2 | AUTH-16 (D) |
| Q3 | Amit | Pastes OTP "482913" | Fills all boxes | Paste rejected | P2 | AUTH-05 |
| Q4 | Priya (dark mode) | Opens app | Readable status bar | Clock/battery invisible | P1 | UI-08 |
| Q5 | Ramesh ji (max font, 320 dp) | Role screen | Full text | Card text clipped | P2 | UI-05 |
| Q6 | Priya | Posts task, taps Post twice | One task | Two tasks | P1 | BRK-03 |
| Q7 | Any customer | Leaves location on default | Real address | "Noida" posted from Kanpur | P1 | BRK-07 |
| Q8 | Helper | Opens app first time | Real jobs or honest empty state | 100 fake "New York" jobs | P0 | BRK-01 |
| Q9 | Helper | Submits a bid of ₹800 | Bid sent | Job silently accepted, ₹800 lost | P1 | BRK-12 |
| Q10 | Ramesh ji | Wants help / call support | WhatsApp/phone help | No help anywhere | P1 | backlog |
| Q11 | Shared family phone | Switch account | Logout/switch | No logout | P1 | AUTH-13 |
| Q12 | Any | Times in chat | IST times | "Just now" everywhere | P2 | BRK-14 |

**Tap count — post a task:** Welcome → Guest (1) → I need help (1) → category (1) → Continue (1) → type
address → Continue (1) → title + budget (typing) → Continue (1) → Post (1) = **7 taps over 4 form screens**.
Recommend merging Photos+Details into Category screen's next step (3 screens) — batch 3 preview.

**Jargon found:** "Verify Your Mobile Number", "Place Bid", "Proposed", "Response Time" — fine for metro,
heavy for Ramesh ji; Hindi/Hinglish copy recommended (D13).

### C. UI/UX per screen
| Screen | Good (keep) | Wrong | Overdone | Missing | Fix |
|---|---|---|---|---|---|
| Splash | Brand colours, logo | 2.5 s wait | Fade + long hold | — | Short / system splash (done) |
| Welcome | Big illustration, guest option ✓ | Two competing CTAs | — | Language choice, policy links | Batch 1 |
| Sign in / Create | Clear fields | Passwords, 5 buttons | Email + Google + OTP + password | Number validation | Batch 1 |
| Mobile / OTP | +91 shown | No number shown on OTP; overflow at 320 dp | — | Autofill, paste | Done + batch 1 |
| Profile setup | Optional photo | 7 fields | Gender/DOB/state/language | — | Name only (D9) |
| Permissions | Clear explanation | Asks nothing | Whole screen | — | Remove (D8) |
| Role selection | Strong design, Hindi toggle ✓ | Fixed card height | Canvas skyline/clouds (cheap, OK) | Saved language | Done (height) |
| Task category | Search ✓, clear grid | 10 sp text | — | — | Done |
| Task location | Address suggestions | Fake GPS default | Map placeholder box | Real map / radius | Done (interim) |
| Task photos/details | Tips card | Long screen mixing photos + text | — | — | Batch 3 |
| Review | Edit links per card ✓ | — | — | — | — |
| Helper home | Skeleton loader ✓ | Fake stats, name | 5 summary cards + performance card | Real empty state | Batch 2 |
| Map | — | Not a map | — | — | D7 |
| Tasks | Status chips ✓ | Dead icons | 7 chips | — | Done (labels/search) |
| Chat | Quick replies ✓ | Fake customer msg | 4 dead quick actions | Customer side | Backend |
| Bottom nav | 4 tabs ✓ | Labels hidden on unselected tabs (icons alone confuse) | Floating pill | Labels | Fixed (labels on all tabs) |

Consistency: colours are partly central (`ui/theme/Color.kt`) but many screens hardcode `Color(0xFF…)`;
corner radii 8/12/16/18/24/32 dp mixed; type sizes set inline everywhere → recommend a design-token pass
(after batches are approved).

---

## 4. Missing-features backlog
**Must-have (before Play release):** backend + real accounts (OTP + Google); customer "My tasks" + offers;
real bids; two-way chat; real location; logout; delete account (in-app + web link); Privacy Policy,
Terms, Contact/WhatsApp help; DPDP consent notice; crash reporting; push notifications (FCM, with
Android 13 permission asked in context); database migrations.
**Should-have:** Hindi + Hinglish UI; payments (UPI-first) if the business takes money; ratings &
reviews; helper verification (KYC); FAQ; app version/about; offline banner + retry.
**Nice-to-have:** invoice PDF (₹ + Hindi fonts); rate-the-app; in-app update prompt; tablet two-pane.

## 5. Needs my decision
| # | Decision | My recommendation |
|---|---|---|
| D1 | Backend: Firebase (Auth + Firestore + Storage + FCM) vs Supabase vs custom server | **Firebase** — fastest path, Indian phone OTP built in, free tier fits launch |
| D2 | Drop passwords & "Forgot password"; only mobile OTP + Google | **Yes** |
| D3 | OTP provider: Firebase Phone Auth vs MSG91/2Factor (needs TRAI DLT registration) | Firebase first; switch if SMS costs grow |
| D4 | Brand name: "FixoraX" (app) or "Fixhora" (repo/design) | Pick one before store listing |
| D5 | Final package name (permanent on Play), e.g. `in.fixhora.app` | Choose now, before first upload |
| D6 | Money: do customers pay helpers inside the app (commission/escrow) or cash? | Start cash; add Razorpay UPI later. Play Billing not needed for real-world services |
| D7 | Add Google Location + Maps SDK (new dependency, Maps API key with billing) — or hide the Map tab until then | Hide Map tab now; add location (no map) first |
| D8 | Remove onboarding Permissions screen; ask location/notifications when needed | **Yes** |
| D9 | Profile: ask only name (single name allowed) + optional city | **Yes** |
| D10 | Policy texts (Privacy, Terms, Refund, Contact, grievance officer) — you provide/approve the legal text | Needed before launch |
| D11 | Crash reporting/analytics service (e.g. Firebase Crashlytics) | Yes, with D1 |
| D12 | Database migrations policy (stop wiping data on update) | Yes, before first release |
| D13 | Hindi translations — who writes/approves them | You/native speaker approve; I can draft |
| D14 | Guest mode: may guests post tasks, or login required to post? | Browse as guest, login to post/accept |

## 6. Previews (mockups)
See `docs/audit/mockups/index.html`.
- **Batch 1 — Login** (AUTH-02, -05, -07, -11): waiting for your OK
- **Batch 2 — Account / delete account, honest helper home, customer "My tasks"** (AUTH-13, SEC-05, BRK-11, PAY-04, BRK-17): waiting for your OK

### Round 2 fixes (no decision needed)
- Helper Chat list: search and chips now filter; "Explore Nearby Jobs" opens Home; dead Filter icon removed
- Helper Tasks: Sort menu; Home: dead Refresh replaced by "New jobs will appear here automatically"; bell says "No notifications yet"
- Bottom navigation shows every tab's name
- "+91" visible in number fields before tapping
- Tablet width cap (UI-10)

## 7. Verification (after fixes, commit `936a7c6`+)
| Check | Before | After |
|---|---|---|
| `assembleDebug` | ❌ fails (debug.keystore missing) | ✅ |
| `assembleRelease` (R8 on) | ❌ fails (corrupted icons / keystore) | ✅ 1.9 MB unsigned APK |
| Unit tests | 1 placeholder test | ✅ 13 pass (number cleaning, ₹ format, IST time, task posting, sample-data seeding, screenshots) |
| Android Lint | 0 errors, 47 warnings | 0 errors, 47 warnings (40 are "newer library version available") |
| 16 KB page size | — | ✅ aligned |
| PDF script | — | 0 PDFs, nothing broken |
| Screenshots | `mockups/before/` | `mockups/after/` — see `mockups/index.html` |

Not verifiable here (no phone/emulator): everything in `MANUAL_TEST_CHECKLIST.md`.
