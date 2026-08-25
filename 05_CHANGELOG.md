# Changelog

## Batch 10 — Close the verification loop

Batch 9 went green and I reported the migration as proven. Auditing that claim afterwards turned up
three loose ends, one of them a hole in my own verification.

**I could not actually prove the tests ran.** The first signal was the step duration: 199s in
Batch 9 against 196s in Batch 8. Three seconds for 36 new Robolectric tests looked impossible, so I
suspected they had been skipped. Reading the log settled it — `> Task :app:testDebugUnitTest`
executed (not `UP-TO-DATE`, not `SKIPPED`) taking 26s, with `33 actionable tasks: 33 executed` — and
also explained the timing: both runs started from a cold Gradle User Home and Batch 9 additionally
downloaded the Gradle distribution, so the totals were never comparable.

The tests ran. But that came from **inference over a log**, not evidence. Gradle prints no test
count on success, so nothing in CI stated how many tests executed. That is a bad place to leave the
project's main safety net: the day a change stops a test class being discovered, the build stays
green and nobody notices.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| CI publishes `test-results` and the HTML report, with `if: always()` | A failing run is exactly when the report is wanted, so it must not be conditional on success. | None. |
| CI writes the test totals into the job summary | The run page now states "N tests in M classes — N failures". This is the check that would have answered my own question in seconds instead of by inference. It only reports and never gates the build, so a mis-parse is visible rather than dangerous. | None. |
| **CI commits `app/schemas` when it changes** | Batch 9 turned on `exportSchema` and uploaded the JSON as an artifact — where it stayed. The proxy in this container blocks the artifact download host, so the file could not be ferried back by hand, and Room's `identityHash` cannot be written by hand either. Having CI commit it keeps the schema in sync with the entities forever rather than relying on somebody remembering. `[skip ci]` stops the push triggering another build; it rebases first in case someone pushed during the run. | Low. Pushes to the working branch, so a local clone goes one commit behind after each schema change. |
| Four `AutoMirrored` icon deprecations fixed | The Batch 9 log flagged `Icons.Filled.Assignment` and `Icons.Filled.HelpOutline` as deprecated. **Two were mine** from Batch 8 (`CustomerFlowContainer.kt`, `MyTasksScreen.kt`); `WorkerHomeScreen.kt` and `TaskReviewScreen.kt` predate it. Auto-mirroring matters for right-to-left layouts, which is live given i18n is on the roadmap. | Low. Identical rendering in a left-to-right layout. |

**Destructive operations: none.** Two CI steps added, four icon references swapped. The schema
commit adds a generated file and never rewrites history. Rollback is reverting this commit.

**What this does not fix:** nothing automated still exercises a Compose screen. The test count now
published is for JVM and Robolectric tests only.

## Batch 9 — Room & migration tests

For eight batches I repeated the same caveat: **`MIGRATION_2_3` had never run against a populated
database.** It is the one code path in this app that can destroy a user's data, and it had zero
coverage — every one of the 77 tests covered logic deliberately kept free of Android imports, so
Room, the DAOs and the migration were untested entirely.

That mattered more than any missing feature. i18n and a real map are things that do not exist; this
is a thing that exists and might be wrong. The migration rebuilds `tasks` by hand — CREATE /
INSERT…SELECT / DROP / RENAME — and hand-writes `users` and three indexes. Room compares what it
finds at open time against what it generated from the entities, and any disagreement throws
`IllegalStateException: Migration didn't properly handle…` **on the user's device, on launch, with
their data already committed to the new shape.** Release has no escape hatch on purpose:
`fallbackToDestructiveMigrationFrom` is restricted to v1 so a bad migration surfaces as an error
rather than as silent data loss.

**Suite: 77 → 113 tests across 16 files.**

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `AppDatabaseMigrationTest` | Builds a real v2 SQLite file — the exact schema recovered from `878835e`, not a guess — populates it with a submitted task, a draft, comma-separated photo URIs and a chat message, marks it `user_version = 2`, then opens it through the **production** `Room.databaseBuilder(...).addMigrations(MIGRATION_2_3)` and reads through a DAO. Reading matters: Room validates lazily, so building the database proves nothing. | Low. Test-only. |
| Asserts what the migration promises | Every row survives with its id; `ownerId` becomes `guest`; `photoUris` converts comma → newline (without that REPLACE every migrated task renders its photos as one broken URI); `createdAt` is recent rather than 1970; `chat_messages` is untouched; `users` works. | Low. |
| Proves the unique index by violating it | Asserting an index exists by name would not prove it is unique. Inserting two accounts with one mobile must throw — `UserDao.insert` uses `OnConflictStrategy.ABORT` precisely so it does. | Low. |
| New `TaskDaoTest` | First coverage for `getTasksForOwner` and `observeTask`, both added in Batch 8 and never run. A wrong `WHERE` here shows one customer another customer's tasks, or leaks an unfinished draft into a list of things they actually asked for. Also checks every `TaskStatus` round-trips through the converter. | Low. |
| New `TaskRepositoryTest` | The draft transactions were written for real bugs in Batches 1 and 3 and never tested: repeated saves must leave one row, submit must promote the draft **in place** rather than copy it, discard must not touch a posted task, and drafts must stay per-owner. | Low. |
| New `ChatDaoTest` | Both chat screens render with `reverseLayout = true` on the assumption the DAO returns newest first. Flip the `ORDER BY` and every conversation reads backwards. Also checks messages stay scoped to their task. | Low. |
| Robolectric, `@Config(sdk = [34], application = Application::class)` | CI has no emulator, so an `androidTest` version of any of this would never run; Robolectric was already a `testImplementation`. The SDK is pinned rather than tracking `compileSdk` — the SQLite behaviour under test does not vary with it, and the first Room test in the project should not also be a bet on Robolectric's newest API support. The plain `Application` override stops `FixhoraApplication` opening the production database and seeding demo data inside every test. | Medium. Robolectric is new to this suite and downloads `android-all` jars on first run. |
| `exportSchema = true` + `room.schemaLocation` | The schema Room expects existed only inside generated code, which is exactly how `MIGRATION_2_3` came to be written against no reference at all. With the JSON committed, a schema change shows up in a diff and the next migration can be checked rather than guessed. | Low. Additive; it only emits a file. |
| CI uploads `app/schemas/**` as an artifact | This container cannot run AGP, so the JSON cannot be generated here. CI produces it and it is committed from the artifact. | None. |

### Honest about one test

`concurrent saves still leave one draft` runs under `runBlocking`, which is single-threaded. It
exercises interleaving at suspension points, not true parallelism — it is a regression guard on the
read-then-write shape, not a proof of thread safety. The comment on the test says so rather than
letting the name imply more than it delivers.

**Destructive operations: none.** Tests create throwaway databases; the migration test deletes only
its own file, in the test's own database directory. The single production change is `exportSchema`,
which writes a JSON file at compile time. Rollback is reverting this commit.

## Batch 8 — The customer's side of the loop

A customer could post a task and then never see it again. The wizard ended on a success screen
whose only exit was "Return to Home", and nothing anywhere showed them what they had asked for,
whether anyone had taken it, or how to reach the person who did. Chat had the matching hole: the
worker could open a thread, the customer had no way in, so every conversation was one-sided.

The database already supported all of it — `ChatRepository.SENDER_CUSTOMER` had existed since
Batch 1. Only the surface was missing.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New **My tasks** tab with everything the customer posted | The core hole. Status, age, location and budget per task, newest first, with an empty state that offers the post flow. | Medium. New screen. |
| New task detail with **the customer's half of the chat** | Messaging lives inside a task rather than in its own tab: a customer's message is always *about* one job. The worker needs a thread list because they deal with many customers; the customer only ever talks about the job in front of them. | Medium. |
| **Cancel a task**, behind a confirmation | There was no way to withdraw a request. Guarded on the row read live from the database, not on what the list showed — a worker may have accepted it in the meantime, and cancelling then would leave somebody working on a task the customer believes is dead. | Medium. Writes a status. |
| `CustomerFlowContainer` with two tabs: Post / My tasks | "I need help" dropped straight into the four-step wizard with nowhere else to go. The wizard itself is unchanged and is still the first tab. | Medium. New navigation layer. |
| System back closes an open task instead of leaving the flow | Without it, back skipped the detail entirely and dropped the customer out of the customer flow, losing the conversation they were reading. | Low. |
| Success screen: **"You will be notified once someone accepts your task"** removed | There are no notifications in this app and never were, so it was a promise the app could not keep. It now says helpers can see it and to check My tasks — which is what actually works — and the button goes there instead of dead-ending at home. | Low. |
| New `customerStatusLabel` / `customerStatusDetail` / `isCancellableByCustomer` / `hasAssignedHelper` | `TaskStatus` names describe the row, not the situation: "SUBMITTED" tells a customer nothing about whether anyone picked their job up. Kept Android-free and covered by `CustomerStatusTest`, including that no status ever offers cancel and chat at once. | Low. Verified by running it. |
| `TaskDao.getTasksForOwner` + `observeTask`; matching repository methods | Drafts are excluded — an unfinished draft belongs to the wizard, not to a list of things the customer actually asked for. The detail reads the row live so a worker accepting or completing it updates the screen the customer is looking at. | Low. |
| `MyTasksViewModel` follows the session rather than reading it once | Signing in or out with the screen open must swap the list, not keep showing the previous account's tasks. | Low. |

### Consolidation (Rule 5)

| Moved | Why |
| :--- | :--- |
| `ui/screens/helper/WorkerFormatting.kt` → `ui/format/TaskFormatting.kt` | It lived in the worker package only because the worker screens were the first to render a task. The customer's list needs the same labels, and one shared copy beats two that drift. Tests moved with it. |
| `MessageBubble` → `ui/components/MessageBubble.kt` | Both halves of one conversation must look like one conversation; a second copy would drift. `isSender` now documents that it means "written by whoever is reading this screen", not "written by the worker". |

**Destructive operations: none.** No schema change — the two new DAO methods are queries. Cancelling
writes `CANCELLED` to an existing row rather than deleting it, so nothing is destroyed and the task
stays in the customer's history. Rollback is reverting this commit.

**Not verified here:** every screen in this batch is Compose, which cannot be compiled or run in
this container. The status logic was executed; the UI is confirmed by `06_MANUAL_TESTS.md`.

## Batch 7 — Accessibility, docs & handover

Last batch: the things that make the app usable by someone who is not holding a mouse, and the
documents that let someone else pick this up.

### Accessibility

Found by reading the code, not by running a scanner. Each row is something a real user hits.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| Photo remove button: touch target **20dp → 48dp**, visible circle unchanged | 20dp is under half the minimum target size. The visible affordance was fine; the tappable area was not, so the fix grows only the hit box. `TaskPhotosScreen.kt` | Low. The 48dp target sits in the corner of an 80dp thumbnail, which is not itself clickable, so nothing is swallowed. |
| That button's icon gained a label | `contentDescription = null` on the **only** content of a clickable means a screen reader announces the control as nothing at all. Now "Remove photo N". | Low. |
| Thumbnails read "Photo N of M" instead of "Uploaded photo" | Every photo carried the identical description, so no two could be told apart — and there was no way to know which one Remove would drop. | Low. |
| "Add another photo" card labelled | Same null-on-only-content problem. | Low. |
| Category cards: `clickable` → `selectable(role = RadioButton)` | Selection was conveyed by border width and tint only. A screen reader cannot see either, so the chosen category was indistinguishable from the rest. | Low. Same visuals, same callback. |
| Worker bottom nav: `clickable` → `selectable(role = Tab)` | Identical problem: the active tab was tint-and-label only. | Low. |
| Three text links reach 48dp and announce as buttons | "Create New Account", "Sign In" and "Resend code" were bare `Text` with `.clickable` — roughly 20dp tall, and announced as text rather than as controls. Padding moved *inside* the clickable so it grows the target rather than the gap around it. | Low. Rows gained `CenterVertically` so the adjacent label stays aligned. |
| **Deleted `ExampleInstrumentedTest.kt`** | It asserted `appContext.packageName == "com.example"`. `applicationId` has been `com.fixhora.app` since Batch R, so this template test would **fail** for anyone running `connectedAndroidTest` — a broken test nobody was running. | Low. |
| **Deleted `ExampleUnitTest.kt`** | Asserted `2 + 2 == 4`. | None. |

Icons that are decorative next to their own label were left with `contentDescription = null` —
that is correct, and relabelling all 46 of them would make screen-reader output worse, not better.

### Documentation

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| **`02_AUDIT.md` rewritten** | It described the app as delivered and had become actively misleading: it called for Retrofit setup (removed as unused), reported Room as absent (it is the whole data layer), and cited line numbers in files since rewritten. Now every original finding is tracked to its current state with the commit that changed it, plus the findings that were never in the original audit — the corrupt launcher icons, the seeding race, the clone-breaking signing config. | None. |
| **`03_GAPS.md` rewritten** | It recommended a backend the project deliberately does not have, and listed as "missing" things that now exist (ViewModels, auth, the worker dashboard, Room, CI). Restructured around the one decision that drives everything: local-only. Separates what is buildable today, what is genuinely blocked on a server, and what is deliberately not being done — and why. | None. |
| **New `06_MANUAL_TESTS.md`** | 58 numbered checks (73 after Batch 8) across six areas, both roles, happy and failure paths, with a Pass/Fail column. Opens with the install-and-update sequence, because that is what has broken most often. Ends with the known limitations that should **not** be filed as bugs. | None. |
| `README.md`: testing section, fuller limitations | The honest split was undocumented — 77 unit tests cover Android-free logic and run in CI; nothing automated touches a Compose screen, a Room migration, or a device. | None. |

### What is still not verified

`Migration 2→3` has never run against a populated database, and no automated test exercises a
Compose screen. Both are named in `03_GAPS.md` rather than left implied.

**Destructive operations: none.** Two dead test files deleted; no schema change, no user data
touched. Rollback is reverting this commit.

## Batch U — In-app update checker (real over-the-air updates)

Reported as "app kyu nahi update ho rahi hai over the air". Two separate findings, both verified
rather than guessed:

1. **There was no update mechanism at all.** Nothing in `app/src/main/java` ever looked at the
   releases page. The delivery chosen in Batch R was a manual one — open Releases, download, tap —
   so the app could not learn that a newer build existed.
2. **No published APK had ever reached the device.** The GitHub API reports `download_count: 0` on
   the assets of v1.0.12, v1.0.13 and v1.0.14. Whatever is installed is an older build carrying the
   pre-Batch-R application id `com.aistudio.fixhora.xyzkpa`, which is a *different package* — a new
   APK installs beside it instead of replacing it, so the old icon keeps opening the old app.

Signing and versioning were ruled out, not assumed: all three releases were built after `fdce853`
committed `keystore/fixhora-debug.jks`, the debug `signingConfig` points at that file, versionCodes
run 12 → 13 → 14, and the application id is `com.fixhora.app.debug` in all three. Installing 14
over 13 would have worked. The gap was delivery.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `data/update/UpdateChannel.kt` — tag parsing, channel-aware asset naming, `pickUpdate` | Kept free of Android imports so every decision is unit-testable, following `TaskFilters.kt` / `WorkerFormatting.kt`. A misread version either hides a real update or offers one Android will refuse. | Low. Covered by `UpdateChannelTest` (verified by running it). |
| New `data/update/UpdateRepository.kt` — GitHub releases call and parsing | `HttpURLConnection` + the framework's `org.json`; Batch 0 removed Retrofit/OkHttp/Moshi and one small API call is no reason to bring three dependencies back. The HTTP call is injectable so parsing is testable without a network. | Low. Covered by `UpdateRepositoryTest`. |
| Reads `/releases?per_page=10` and takes the **highest** version, not `/releases/latest` | `latest` is ordered by each release's `created_at`, which is the *tagged commit's* date — and every tag so far points at `main`, so all three share one timestamp and the winner is a tie-break. Taking the maximum is correct whatever order the API returns, and lets a release missing this channel's APK be skipped instead of dead-ending the check. | Low. |
| New `data/update/ApkInstaller.kt` — `DownloadManager` download, `FileProvider` install intent | Survives backgrounding, resumes across connectivity changes, and reports progress. Writes to the app-specific external dir, so no storage permission on any supported API level. | Medium. Untestable here; device-verified only. |
| Explicit `canRequestPackageInstalls()` check before downloading | From Android 8 every installing app needs its own "Install unknown apps" grant. Without it the installer opens and closes again — no error, nothing. This is the single most common reason a sideloaded update silently does nothing. Checking first turns a 25 MB wasted download into one extra tap. | Low. |
| New `ui/screens/update/UpdateViewModel.kt` + `UpdateDialog.kt` | A dialog, not an inline banner, so it works over every screen without any of them changing layout to make room. Nothing is drawn in the idle case. | Medium. New surface above the NavHost. |
| Automatic check throttled to once per six hours; silent when up to date or when it fails | Unauthenticated GitHub allows 60 calls an hour, and an automatic check that announces "you are up to date" on every launch is noise. A check the user asked for always answers. | Low. |
| `AndroidManifest.xml`: `REQUEST_INSTALL_PACKAGES` + `FileProvider` with `${applicationId}.fileprovider` | Without the permission the system refuses to open the installer. The authority is templated so debug and release do not both claim one authority — two apps that do cannot coexist on a device. | Low. |
| `SessionManager`: `lastUpdateCheckAt` / `recordUpdateCheck` | Deliberately kept out of `Session`: it is updater bookkeeping, not something screens should recompose on. | Low. |
| Version footer on `RoleSelectionScreen` is now tappable and forces a check | The automatic check is throttled; this is how you ask right after a build is published. Padding raised to a real touch target. | Low. |
| CI: `gh release create --target "$GITHUB_SHA"` | Every tag so far was created on `main` rather than on the commit that was built, so the tag misreported its own commit and all releases shared a `created_at`. | Low. |
| CI: build fails if the computed `versionCode` is **lower** than the highest published one | A shallow clone or a branch with fewer commits computes a smaller commit count, and Android refuses to install a lower versionCode — permanently breaking the update path for anyone on the higher build. Equal is allowed so re-running a commit can republish. | Low. |
| `testImplementation(libs.org.json)` | `android.jar` stubs `org.json` for unit tests: every call throws `Stub!`. The real implementation has to be on the test classpath or the parsing tests exercise nothing. | Low. Test-only dependency. |
| Corrected two stale comments (`app/build.gradle.kts`, `README.md`) claiming debug builds use the machine-local keystore | Untrue since `fdce853` committed a shared debug keystore. | None. |

**Destructive operations: none.** No schema change, no migration, no file the user owns is touched
— the only deletion is this app's own previously downloaded APK in its own external files
directory, before a new download replaces it. Rollback is reverting this one commit; nothing on an
already-installed device is affected.

**Known limitation:** this ships in 1.0.15, so the *current* install still has to be updated by
hand once — and if the pre-Batch-R `com.aistudio.fixhora.xyzkpa` build is still on the device it
must be uninstalled that one time, because a different application id is a different app.

## Batch 6 — Worker screens: honest & functional

Every number on the worker dashboard was invented, and most of its controls did nothing.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| New `TaskFilters.kt`: `TaskTab` enum, search matching, `HelperStats` | The tabs were strings matched in a `when` that ended `else -> false`, so "In Progress" and "Cancelled" could never show anything, and a "Pending" tab existed for a status no task ever had. | Low. Covered by `TaskFiltersTest`. |
| New `WorkerFormatting.kt`: relative time, poster name, budget, location | Replaces strings that were printed regardless of the data behind them. | Low. Covered by `WorkerFormattingTest`. |
| `HelperViewModel` derives stats, search and filter state from the database | The screens held their own `remember` state, so a typed query had nowhere to go. | Medium. |
| **Removed the "Performance Overview" card** | ₹1,250 earnings, 142 jobs completed, a 4.9★ rating, "< 5m" response time and 95/98/96% rings — none had any data behind it. Payments, ratings and response times are not tracked. Replaced with three counts the app can prove: open nearby, active, completed. | Low. |
| Summary tiles (3/1/2/5/0) now count real statuses | The five numbers were literals. | Low. |
| Job cards show the real poster, age, address and budget | Each card read "Customer Name" with a verified tick and a 4.8 rating, "10 mins ago", and "2.5 km away" for anything posted with the location switch on. Distance needs the helper's own position, which is not tracked, so the address is shown instead. | Low. |
| Search wired on Tasks, Chat and Browse | All three accepted typing and filtered nothing. | Low. |
| "Chat" on a job card opens that conversation | It was an empty lambda. It now switches to the Chat tab with the thread open. | Medium. New cross-tab navigation. |
| "Navigate" opens a `geo:` intent | Was an empty lambda. Needs no API key and works with any installed maps app; a device with none is a no-op, not a crash. | Low. |
| Task actions follow the real lifecycle | Accept → Start → Done, each writing a real status. `startTask` finally has a control, so the "In Progress" tab can populate. | Low. |
| **Removed:** "Place Bid" / "Submit Proposal", top-bar Search/Filter/Sort, notification bell + "2" badge, Chat tab "3" badge, online/offline pill, Call, More, Attach, voice note, "0 bids", "Urgent" | None was implemented. Bidding, notifications, presence and read receipts do not exist in this app; the badges were literals. | Medium. Visible controls disappear. |
| The Map tab became a category browser | Markers sat at fixed screen offsets (`index * 20.dp`) with `val isUrgent = true // Mock logic`, presented as geography. There is no maps SDK, so the screen now filters real jobs and says plainly that the map is not available yet. | Medium. Largest visual change. |
| Chat: no more injected greeting; real timestamps; empty state | Every conversation opened with a fabricated "Hello! I saw your job request…" attributed to the customer, and every message read "Just now". | Low. |

## Launcher icon fix

The app had no launcher icon, despite a full icon set being in the repository.

| Changed | Reason | Risk |
| :--- | :--- | :--- |
| Replaced all 20 launcher PNGs in `app/src/main/res/mipmap-*/` | **Every one of them was corrupt.** Each began `ef bf bd 50 4e 47` instead of `89 50 4e 47` — the leading `0x89` byte rewritten as the UTF-8 replacement character, which is what happens when a binary file is read as text and written back. Android could not decode any of them, so the launcher fell back to a default icon. The valid originals were sitting in `android/res/`, which Gradle never compiles. | Low. |
| Added `ic_launcher_round.png` for every density | The round icon existed only as `mipmap-anydpi-v26/ic_launcher_round.xml`, which covers API 26+. `minSdk` is 24, so on API 24–25 `@mipmap/ic_launcher_round` resolved to nothing at all. | Low. |
| `isCrunchPngs = true` for release | This is why the corruption shipped silently. With crunching off, aapt copies PNGs through byte-for-byte without decoding them, so twenty unreadable files passed the build. With it on, a malformed PNG fails the build instead of becoming a blank icon on someone's home screen. | Low. Slightly slower release builds. |
| Deleted `app/mipmap-hdpi/` | Stock template icons sitting outside any source set — never built, and easy to mistake for the real ones. | Low. |

The in-app artwork (`img_logo`, `img_customer`, `img_worker`) was never affected, which is why the splash and role screens looked right while the launcher did not.

`android/`, `ios/` and `web/` remain as the multi-platform icon export; only `app/src/main/res/` is compiled into the APK.

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
