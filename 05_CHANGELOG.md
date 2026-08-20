# Changelog

## Batch 3 — Task flow correctness

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `TaskPhotoStore`: picked photos are copied into app storage | **The planned fix was wrong.** The plan said to call `takePersistableUriPermission` on the picker's URI, but the Android photo picker grants read access only "until the device restarts or your app stops" and those grants are *not* persistable — the call throws `SecurityException` on Android 13+, while quietly succeeding on older devices that fall back to `ACTION_OPEN_DOCUMENT`. Copying is the only fix that works on every supported version, and it also keeps a task's photos if the user later deletes the original from their gallery. | Medium. Photos now consume app storage; see the limitation below. |
| Photo import moved into the ViewModel, with a visible "Saving photos…" state | Copying is file I/O and the picker callback runs on the main thread. | Low. |
| Removing a photo deletes its stored copy | Otherwise every removed photo leaked disk space that nothing would ever reclaim. | Low. |
| New `SubmitState`; the Review screen navigates on success, not on the tap | "Post Task" fired the write and navigated in the same breath, so a failed save still showed "Task Posted successfully!". It now shows a spinner, and on failure an error with a "Try Again" button, keeping the user's input. | Medium. Changes what the success screen means. |
| Review no longer falls back to the first category | `dummyCategories.first()` meant an uncategorised task displayed as "Home Repairs" — the review screen stated something the user had not chosen. Now reads "Not selected" with a link to fix it. | Low. |
| Review no longer invents a title or description | Empty fields rendered as "Need Help with X" / "Looking for someone to help me out", which is a different task than the blank one being reviewed. | Low. |
| Service-area card is a real picker (5 / 10 / 25 / 50 km) | The card was hardcoded to "Within 5 km" and `updateSelectedDistance()` was never called from anywhere, so the field the review screen displayed could never change. | Low. |
| Back press asks before discarding a draft, and discarding cleans up | Leaving the flow was the one place a user could silently lose everything they had typed. Discarding now also deletes the draft row and the copied photos. | Low. |
| Removed the "Skip" action on the Photos step | That screen also holds the required task title, so "Skip" walked straight past its validation and posted an untitled task. | Low. Photos are still optional — the Continue button only requires a title. |
| "Edit" on Review pops back instead of rebuilding the flow | It navigated with `popUpTo(Category) { inclusive = true }`, tearing down and recreating every step. Now it pops to the first step with the rest intact. | Low. |
| Debug sample-photo button gated behind `BuildConfig.DEBUG` | It was visible in release builds, offering real users stock photos of someone else's plumbing as their task's evidence. | Low. |

**Known limitation:** photos are copied at full resolution. Five photos from a modern phone camera can be 20–30 MB of app storage per task. Downscaling on import belongs on the roadmap.

## Batch 2 — Real authentication

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `Validators` + `FieldError`, and `AuthMessages` mapping codes to text | Not one auth field was validated anywhere. Validation returns codes, not sentences, so the rules stay language-free and a Hindi build can swap the messages without touching them. | Low. Pure functions, fully tested. |
| New `AuthViewModel` holding sign-in / sign-up / profile state | The screens held their own `remember` state and had nowhere to put a result, which is why "Sign In" could only navigate blindly. | Medium. New state owner for four screens. |
| Sign-in now actually verifies credentials | The button called `onLoginSuccess` unconditionally: an empty form signed you in. It now checks the password against the stored hash, shows one message for both "no such number" and "wrong password", and navigates only on success. | Medium. Existing testers must now register before they can sign in. |
| Sign-up validates every field, enforces the terms checkbox, and creates a real account | Nothing was validated and no account was created; the terms checkbox was decorative. | Medium. |
| Buttons disable and show a spinner while work is in flight | Nothing stopped a double submit, which with real writes would mean two registration attempts. | Low. |
| Forgot-password steps 2 and 3 are functional | Both steps rendered fields hard-wired to `value = ""` with an empty `onValueChange` — nothing could be typed, and no password was ever changed. It now verifies a code and writes a new hash with a fresh salt. | Medium. |
| OTP is generated, displayed and checked, behind a visible "Demo mode" notice | "Verify" previously navigated without reading the code. There is no SMS gateway, so claiming "OTP sent to your mobile" would leave the user waiting for a message that never arrives. The screen says so plainly and shows the code. | Low. Must be replaced by a real gateway before release. |
| OTP entry auto-advances between boxes and accepts a pasted code | Six single-character fields with no focus handling is unusable on a phone. | Low. |
| OTP for an unregistered number routes to sign-up with the number pre-filled | Verifying a number proves ownership; it does not create an account. Previously it walked into profile setup with nothing to attach the profile to. | Low. |
| Profile setup writes to `UserEntity`, pre-filled from the account | The whole form was discarded on Continue. | Low. |
| Permission screen requests real permissions; manifest declares location and notifications | The screen described permissions and requested none, so "Allow" and "Skip" behaved identically and location could never work. Now it shows granted state, and offers Settings only on a permanent denial. | Medium. First time these dialogs appear. |
| Session-aware start destination, and `popUpTo` on every terminal navigation | Back after signing in returned to the sign-in form, and a signed-in user was sent through Welcome on every launch. | Medium. Touches the whole nav graph. |
| `RoleSelectionScreen` takes a `UserRole`, shows who is signed in, and offers sign out | Roles were raw strings duplicated across files, and there was no way to see or end a session. | Low. |
| Removed the "Continue with Google/Email" buttons and "Keep me signed in" | The social buttons only raised a "Coming soon" toast, and the checkbox did nothing — sessions now always persist. | Low. Dead controls; nothing is lost. |
| Removed `accompanist-permissions` | Batch 0 enabled it for this work, but the platform's own permission launcher covers the case with no extra dependency. | Low. |

**Still demo-only:** OTP codes are generated on-device and shown on screen. This is not authentication against a phone number — it must be wired to a real SMS provider before release.

## Batch 1 — Data foundation & session

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `UserEntity` / `UserDao` / `UserRepository`, with `mobile` uniquely indexed | The app had no concept of a user at all. Profile data collected during sign-up was dropped on the floor and there was nothing to attach a task to. | Medium. New table; exercised for the first time in Batch 2. |
| New `PasswordHasher` — salted PBKDF2-HMAC-SHA256, 100k iterations | Storing a password in a local database in plain text exposes it to anyone with the device or a backup, and people reuse passwords. Implemented over `Mac` rather than `SecretKeyFactory` because `PBKDF2WithHmacSHA256` only exists from API 26 and `minSdk` is 24. | Low. Verified against published PBKDF2-HMAC-SHA256 vectors. |
| New `SessionManager` (DataStore) + `Session`/`UserRole`/`ThemePreference`/`AppLanguage` | Nothing remembered who was signed in, so every restart was an anonymous restart. Guest browsing stays a first-class state. | Low. |
| New `TaskStatus` enum + Room `Converters`, replacing raw status strings | Statuses were free-form strings compared in five different files, which is how `"pending"` ended up as a tab that no task could ever match. Storage values are unchanged, so no data is reinterpreted. | Medium. Touches every status comparison in the app. |
| `TaskEntity` gains `ownerId`, `acceptedByHelperId`, `createdAt`, `latitude`, `longitude`; indexed on `status` and `ownerId` | Tasks had no author, no age and no coordinates — which is why the helper feed had to invent "10 mins ago" and "2.5 km away". Adding the columns now avoids a second migration later. | Medium. Schema change. |
| Photo URIs stored newline-separated instead of comma-separated | A comma is legal inside a URI and would have corrupted the split. Existing rows are converted by the migration. | Low. |
| `AppDatabase` v2 → v3 with an explicit `MIGRATION_2_3` | `fallbackToDestructiveMigration()` was unconditional, so any schema change silently wiped a user's data in release too. `tasks` is rebuilt rather than `ALTER`-ed, because a SQLite DEFAULT that the entity does not declare fails Room's schema validation at open time. | **High — the least verifiable change in this batch.** Debug still falls back destructively, so a mistake shows up as a wipe in testing, not in release. |
| `insertDummyData()` (100 fake tasks) replaced by `DemoDataSeeder` (10, Indian context) behind `BuildConfig.DEBUG` | Release builds were seeding 100 fake tasks. Both ViewModels also called it on init, so a race could double the data set. Seeding now happens once, from `FixhoraApplication`, in debug only. | Low. Release feed now starts genuinely empty. |
| `saveDraft` / `submitTask` wrapped in `withTransaction`; submit promotes the draft row in place | Submit previously inserted a copy and then deleted the draft in two unguarded steps, so a failure in between left an orphan. Promoting in place means the task never exists twice. | Low. |
| `TaskRepository.completedTasks` renamed to `openTasks` | The name said "completed" while the query returned `status = 'submitted'` — the opposite of what it fetched. | Low. |
| Pulled forward from Batch 3: `submitTask()` now cancels the pending autosave | A debounced save landing after submission recreated the draft the user had just posted. The fix is one line inside a function this batch rewrote, so holding it back would have meant knowingly shipping the bug. | Low. |
| `WorkerTasksScreen` tabs mapped to real statuses; dead `"Pending"` tab removed | `"In Progress"` and `"Cancelled"` fell through to `else -> false` and could never show anything; no task ever had status `"pending"`. | Low. Wiring a control that *sets* `IN_PROGRESS` is Batch 6, so that tab stays empty until then. |

**Known, deliberately not fixed in this batch:** `acceptedByHelperId` is written by nothing yet (needs helper identity, Batch 6), and `HelperViewModel.startTask` has no button wired to it yet (Batch 6). Both are schema/API groundwork that would otherwise need a second migration.

## Batch 0 — Build hygiene & dependency cleanup

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| Added `gradlew`, `gradlew.bat`, `gradle/wrapper/` (Gradle 9.1.0) | Repo had no wrapper at all, so it could only be built by whatever Gradle version a machine happened to have. Now `./gradlew assembleDebug` works from a clean clone and CI is possible. | Low. If Gradle 9.1.0 turns out to be wrong for AGP 9.1.1, only `gradle-wrapper.properties` needs a version bump. |
| Debug build no longer uses the custom `debugConfig` signing config | It pointed at `${rootDir}/debug.keystore`, which is gitignored and not in the repo — `assembleDebug` failed on any fresh clone. AGP's auto-generated `~/.android/debug.keystore` is used instead. | Low. Debug APKs get a different signature than before, so an existing debug install must be uninstalled before the new one installs. |
| Release signing only applied when the keystore file actually exists | `assembleRelease` previously hard-failed on a missing `my-upload-key.jks`. Now it produces an unsigned release build instead of erroring. | Low. A real release still signs exactly as before once `KEYSTORE_PATH`/`STORE_PASSWORD`/`KEY_PASSWORD` are set. |
| Removed Retrofit, OkHttp, logging-interceptor, Moshi (+ its KSP processor), converter-moshi, Firebase BOM | Zero references anywhere in `app/src`. They pulled an extra KSP round and Firebase's BOM into every build for nothing. | Low. Nothing imports them. Re-adding is a catalog entry away if a backend lands. |
| Enabled `play-services-location`, `datastore-preferences`, `accompanist-permissions` | Needed by Batch 1 (session) and Batch 4 (real GPS). | Low. |
| `rootProject.name`: `"My Application"` → `"Fixhora"` | Template leftover; made the Android Studio project name meaningless. | Low. |
| `.env.example`: removed `GEMINI_API_KEY` | Gemini is not used anywhere in the app. The Secrets plugin reads this file, so a fake key was being compiled into `BuildConfig`. | Low. File is kept (the plugin requires it) and documents the expected format. |
| `metadata.json`: dropped the `SERVER_SIDE_GEMINI_API` capability claim | The app makes no Gemini calls. | Low. |


## [WP-001] Implement TaskViewModel
**Status:** Completed
**Description:** Created a `TaskViewModel` state holder to persist task draft selections across the task creation flow.
**Files Modified:** `TaskFlowContainer.kt`, `TaskCategoryScreen.kt`, `TaskLocationScreen.kt`, `TaskPhotosScreen.kt`, `TaskReviewScreen.kt`, `TaskViewModel.kt`
**Outcome:** State survives screen transitions within the flow in `TaskViewModel`.

## [WP-002] Wire Up Review Screen
**Status:** Completed
**Description:** Updated `TaskReviewScreen` to fetch data from the `TaskViewModel` instead of using hardcoded mock data. 
**Files Modified:** `TaskReviewScreen.kt`, `TaskCategoryScreen.kt` (extracted shared CategoriesData.kt).
**Outcome:** Review screen displays the exact category, location, and details entered by the user.

## [WP-003] Fix Helper Flow Dead End
**Status:** Completed
**Description:** Implemented a placeholder 'Coming Soon' screen for the helper flow.
**Files Modified:** `RoleSelectionScreen.kt`, `FixhoraApp.kt`, `HelperComingSoonScreen.kt` (new)
**Outcome:** Tapping "I want to help" navigates to a designated helper coming soon screen.

## [WP-004] Functional Inputs & Validation
**Status:** Completed
**Description:** Made category search and location text inputs functional. Added validation barriers for empty inputs before proceeding to next steps.
**Files Modified:** `TaskCategoryScreen.kt`, `TaskLocationScreen.kt`
**Outcome:** Search actively filters categories. User cannot proceed without selecting a category or satisfying location prerequisites.

## [WP-005] Photo Picker Integration
**Status:** Completed
**Description:** Replaced mock photo upload boxes with a real `PickMultipleVisualMedia` image picker implementation using Coil for loading.
**Files Modified:** `TaskPhotosScreen.kt`, `build.gradle.kts`
**Outcome:** Users can pick images from the device gallery and preview them in the app.

## [WP-006] Extract Hardcoded Strings
**Status:** Deferred
**Description:** Extracting strings to `strings.xml`. Deferred to prioritize core functionality like data persistence.

## [WP-007] Room Database Implementation
**Status:** Completed
**Description:** Configured Room database with `TaskEntity`, `TaskDao`, and `AppDatabase`.
**Files Modified:** `build.gradle.kts`, `FixhoraApplication.kt` (new), `TaskEntity.kt` (new), `TaskDao.kt` (new), `TaskRepository.kt` (new), `TaskViewModel.kt`, `TaskFlowContainer.kt`.
**Outcome:** Task drafts are now auto-saved to Room as users iterate through the steps. Force closing the app and reopening restores the user's progress. Submitting saves it permanently.

## [WP-008] Fix Interaction Channel Crashes
**Status:** Completed
**Description:** Addressed application crashes causing `Channel is unrecoverably broken` errors.
**Files Modified:** `AndroidManifest.xml`, `TaskViewModel.kt`
**Outcome:** Added `INTERNET` permission for Coil. Added debounce & exception handling to Room DB transactions since keystroke state updates were overwhelming the main dispatch thread with synchronous inserts. Fully-qualified the `<application>` name in the manifest to ensure correct instantiation Context.

## [WP-009] Functional Provider Dashboard
**Status:** Completed
**Description:** Added functional implementation for the Provider Dashboard, replacing the mock screen.
**Files Modified:** `HelperDashboardScreen.kt`, `HelperViewModel.kt`, `FixhoraApp.kt`, `HelperComingSoonScreen.kt` (deleted)
**Outcome:** The provider flow now actively queries and displays the submitted tasks from the Room database in a feed. Requests made via the 'Need Help' persona are actively visible in the 'I want to help' persona.

## [WP-010] Fix Startup Crash
**Status:** Completed
**Description:** Addressed an unrecoverable Channel broken exception caused by schema evolution.
**Files Modified:** `FixhoraApplication.kt`
**Outcome:** Added `fallbackToDestructiveMigration()` to Room database builder so the app cleanly drops and rebuilds the SQLite tables instead of deadlocking the launch activity window when local Entity classes change shapes.

## [WP-011] Onboarding UI Redesign
**Status:** Completed
**Description:** Complete UI revamp for the Onboarding / Role Selection Screen using Material 3 and custom Compose Canvas graphics.
**Files Modified:** `RoleSelectionScreen.kt`, `Color.kt`
**Outcome:** Replaced default styling with the specified flat vector aesthetics, updated primary color palettes, added skyline and cloud background canvases, and introduced the custom split-color Fixhora App Logo rendering logic.
