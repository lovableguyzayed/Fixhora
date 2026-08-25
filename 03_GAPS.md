# Gap Analysis — what is genuinely missing today

Rewritten against the current codebase. The original version listed gaps that have since been
built (ViewModels, auth, the worker dashboard, Room, CI) and recommended a backend the project
deliberately does not have. Those rows are gone rather than left to mislead; `02_AUDIT.md` records
what happened to each.

**The governing decision:** this app is **local-only**. Room on the device is the single source of
truth, there is no server, and nothing syncs between devices. Most of the gaps below follow from
that one choice, so the honest ordering is "what a local app still owes its user" first, and
"what would need a backend" second.

## 1. Missing, and buildable without a backend

| Gap | Why it matters | Priority |
| :--- | :--- | :--- |
| **Full i18n** | The Hindi/English pill sets a stored preference and changes nothing else. Strings are hardcoded in Compose files, so nothing is translatable yet. Extracting to `strings.xml` is step one and only pays off with step two. | Must-have |
| **Compose UI tests** | Batch 9 covered Room: the migration, the DAOs and the repository transactions now run under Robolectric (16 files, 113 tests). What is still untested is the UI — nothing exercises a Compose screen, so every layout, navigation and accessibility claim rests on the manual script. | Should-have |
| **Real map** | `WorkerMapScreen` is a category browser and says so. A real map needs `maps-compose` and a Maps SDK key. | Should-have |
| **Notifications** | Nothing tells a worker a job was posted while the app was closed. Local notifications could cover the on-device cases; anything cross-device cannot work without a server. | Should-have |
| **Tablet / landscape layout** | Single-column layouts stretch rather than reflow. Window size classes would fix it. | Nice-to-have |

**Built since:** customer task history and the customer half of chat both landed in Batch 8.

## 2. Missing, and blocked on a backend that does not exist

Listed so the boundary is explicit — not as a plan.

| Gap | What it would take |
| :--- | :--- |
| **Multi-device sync** | Every account and task lives only on the device that created it. Reinstalling loses everything. This is the single biggest consequence of local-only. |
| **Real OTP** | There is no SMS gateway. The OTP screen says "Demo mode — no SMS is sent" and shows the code in debug builds rather than pretending one was sent. |
| **Bidding / offers** | "Place Bid" was removed rather than faked. A marketplace needs a server to arbitrate. |
| **Ratings and reviews** | Removed from the worker dashboard for the same reason — a 4.9★ with nothing behind it is worse than no rating. |
| **Payments** | Not started. The budget fields are a number the customer types, nothing more. |
| **Server-side identity** | Passwords are hashed correctly (PBKDF2, 100k iterations, per-user salt) but only locally. There is no account recovery beyond the on-device reset. |

## 3. Deliberately not doing

| Item | Reason |
| :--- | :--- |
| **Dependency injection framework (Hilt)** | `FixhoraApplication` is the composition root and hands repositories to a handful of ViewModel factories. At this size Hilt would add build time and indirection without removing any real problem. |
| **Retrofit / OkHttp / Moshi** | Removed in `0d1ecd0` because nothing imported them. The app makes exactly one network call — the update check — and `HttpURLConnection` with the framework's `org.json` covers it. |
| **Dynamic colour** | The blue/orange pairing is the product's identity, and letting the wallpaper recolour it would also discard the contrast guarantees `ContrastTest` enforces. |
| **`namespace` rename from `com.example`** | Renaming every Kotlin package is churn with no user-visible effect. `applicationId` — the part that actually identifies the app — is already `com.fixhora.app`. |

## 4. Release readiness

| Item | State |
| :--- | :--- |
| Debug channel | **Working.** Every push publishes a debug-signed APK; the app updates itself from it. |
| Release channel | **Not enabled.** Needs `./scripts/make-release-keystore.sh` run by the owner and four repository secrets: `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`. Until then CI builds and publishes the debug APK only. |
| Play Store | **Not viable yet**, and not only for signing: i18n, a privacy policy, and a data-safety declaration for the location permission are all prerequisites. |
| ProGuard / R8 | `isMinifyEnabled = false`. Turning it on needs Room and Compose keep rules verified on a device first. |
