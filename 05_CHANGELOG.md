# Changelog

## Batch R — Release signing, versioning & APK delivery

Requested out of sequence: an installable APK, where **a newer APK always installs over an older
one**. Three separate things made that impossible.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| Release signing reads a keystore from `RELEASE_KEYSTORE_*` environment variables | Android refuses an update signed with a different key than the installed app. Distributed APKs now all share one key, held in GitHub Secrets. Batch 0's debug default was the machine-local `~/.android/debug.keystore`, so an APK built anywhere else could never update one built here. | Medium. |
| `assembleRelease` fails loudly without a keystore | Batch 0 made it fall back to unsigned so a fresh clone would not break. An unsigned APK installs nowhere, so producing one silently is worse than stopping. The guard only trips when a release task was actually requested, so `assembleDebug` and `test` still work with no secrets. | Low. |
| `versionCode` derived from `git rev-list --count HEAD` | It was hardcoded to `1`, so Android could not tell one build from the next and had no upgrade semantics at all. Now it rises with every commit, with no manual step. Overridable with `-PversionCode=N`. | Medium. **CI must use `fetch-depth: 0`** — a shallow clone counts fewer commits, produces a *lower* version, and Android rejects that install outright. |
| `applicationId`: `com.aistudio.fixhora.xyzkpa` → `com.fixhora.app` | An AI Studio leftover that would have become the app's permanent identity on the Play Store. Changing it is only cheap before real distribution. | **Requires one uninstall.** Android sees a new id as a different app, not an update. |
| Debug builds get `.debug` suffix and a "FixoraX (Debug)" label | Debug is signed with a machine-local key and release with the CI key, so one can never update the other. Separate ids let both sit on the device instead of the install failing. | Low. Reverses a Batch 0 decision that no longer holds now that release has its own key. |
| `app_name` moved from `strings.xml` into per-build-type `resValue` | Needed for the debug label. Declaring it in both places is a duplicate-resource error. | Low. |
| Running version shown on the role-selection screen | Otherwise there is no way to tell whether an update actually landed. | Low. |
| New `.github/workflows/release-apk.yml` | The APK has to come from somewhere: this container cannot build it (`dl.google.com` is blocked by network policy, so the Android SDK and AGP are both unreachable — verified in Batch 0). CI runs the tests, builds, verifies the APK is genuinely signed with `apksigner`, and publishes it to a GitHub Release. | Medium. First CI run is also the first real compile of Batches 0–5. |
| New `scripts/make-release-keystore.sh` | The signing key is unrecoverable — lose it and the app can never be updated again under this id. The script generates it **on the user's own machine** so the private key never passes through a chat transcript. | Low. |
| `.gitignore`: `*.jks`, `*.keystore` | A signing key in the repository can be used by anyone to sign an APK that Android accepts as this app. | Low. |
| `README.md` rewritten | It was AI Studio boilerplate about a Gemini API key, describing a different project. Now covers install, update, the "App not installed" causes, signing, versioning and architecture. | Low. |

## Batch 5 — Design system & dark mode

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `Palette.kt` holding every colour as plain `Long` ARGB, with no Android import | Keeping the palette Android-free is what makes `ContrastTest` an ordinary JVM test. `Color.kt` is now the only file that wraps these for Compose. | Low. |
| New `Contrast.kt` + `ContrastTest` asserting WCAG AA across both themes | An unreadable colour can no longer be added without a test going red. | Low. |
| **Four contrast failures found and fixed by running the check** | Hand-computed values in the plan were right on white but wrong on tinted surfaces. `#EF6C00` warning text scored 4.45:1 on its own surface and `#D32F2F` danger scored 4.36:1 — both just under AA, and both used precisely on those surfaces. Darkened to `#A84B00` (5.25:1) and `#C62828` (4.92:1). | Low. |
| Brand orange split into three roles | `#FF6B00` measures 2.86:1 on white — it fails even the 3:1 required of a meaningful icon. `accent` keeps the brand colour for decoration, `accentGraphic` (`#E85D00`, 3.48:1) is for icons that carry meaning, `accentText` (`#C25100`, 4.70:1) is for anything readable. | Low. |
| Dark theme is a designed palette, not an inversion | `#0B57FF` scores 2.97:1 on the dark surface. Dark mode uses `#7EA6FF` with dark text on top (6.83:1). | Low. |
| `MyApplicationTheme` follows the system and the user's stored preference | `darkTheme = false` was hardcoded, so `DarkColorScheme` was unreachable dead code. | Medium. |
| `onSurface` mapped to `textPrimary` instead of a grey | It pointed at `SecondaryGrey`, quietly rendering every unstyled body string in the app as low-contrast grey. | Low. |
| Full type scale, 4dp spacing scale, shared radii | Only `bodyLarge` was defined; everything else was commented out and each screen hardcoded its own sizes. | Low. |
| New `ui/components/`: `FixButton`, `FixCard`, `StatusBadge`, `EmptyState`, `SkeletonBox`, `SectionHeader` — all adopted | Every screen hand-rolled `Button(height = 56.dp, shape = RoundedCornerShape(12.dp))` and none handled loading. All six are in use; none was left as speculative API. | Medium. |
| ~200 hardcoded colours swept to tokens across 15 screens | A `Color.White` written into a screen stays white however the theme changes — this is what made dark mode unreachable in practice, not just in `Theme.kt`. | **High. The largest mechanical change in the project.** |
| `values-night/colors.xml` + window background | Opening the app in dark mode flashed a white launch window before Compose drew. | Low. |

**Two bugs the sweep itself introduced, caught by re-reading the diff:**
- `PerformanceOverviewCard` had a `DarkNavy` container, which the sweep rewrote to `textPrimary` — white in dark mode, with white text on it. Now uses `primary` with `onPrimary` content.
- Its progress ring was `primary` on that same card, so it would have been invisible once the card became primary-coloured. Now `onPrimary`.

**Known limitation:** the star-rating gold and one purple summary tile are still hardcoded. They sit inside the fabricated worker statistics that Batch 6 removes outright, so tokenising them would have been work spent on code about to be deleted.

## Batch 4 — Real location

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `LocationProvider` over `FusedLocationProviderClient` + `Geocoder` | GPS was never requested anywhere in the app. Every one of the four ordinary outcomes — permission refused, location switched off, no fix indoors, no geocoder backend — returns a distinct result instead of an exception, because the user needs to be told which one happened. | Medium. First real use of Play Services location. |
| "Use my current location" resolves a real position | The switch previously just flipped a boolean while the screen displayed a hardcoded `"Sector 62, Noida, Uttar Pradesh 201309"`. That string then travelled into the review screen and the helper feed as though it were the user's address. | Medium. |
| Turning the switch on requests the location permission if needed | Nothing in the flow had ever asked for it, so location could not have worked even if it had been wired up. | Low. |
| Each failure gets its own message, and "Location is off" offers a Settings shortcut | "Something went wrong" would leave the user with no idea whether to grant a permission, turn on GPS, or walk to a window. | Low. |
| Address suggestions come from `Geocoder.getFromLocationName` | **Changed from the plan.** The plan said to label the mock list as "Demo suggestions"; a real lookup was available for free through the platform geocoder, so the five hardcoded Indian addresses are gone rather than relabelled. When the device has no geocoder backend the list is simply empty — nothing is fabricated. | Medium. Depends on a device geocoder; results vary by device and network. |
| Address search is debounced (450 ms) | Otherwise every keystroke fires a geocoder lookup. | Low. |
| Picking a suggestion stores its coordinates; typing clears them | Coordinates left over from a previous fix would otherwise stay attached to a different, newly typed address. | Low. |
| Review screen shows the real address | It printed "Current Location (Noida)" whenever the switch was on, wherever the user actually was. | Low. |
| Map graphic labelled "Map preview — not an interactive map yet" | It reads as a real map with a pin dropped at the user's address. It is neither. | Low. |
| Continue accepts coordinates without an address | A fix that resolves but cannot be reverse-geocoded still locates the task; blocking it would strand the user. | Low. |

**Known limitation:** the map is still a static graphic. Real map rendering needs the Maps SDK and a billing-enabled API key, which this project deliberately does not have.

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
