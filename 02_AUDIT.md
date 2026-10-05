# Forensic Audit — findings and where they stand

The original audit was taken against the codebase as delivered. Every row below is the finding as
it was written, plus what actually happened to it. "Fixed" means the change is in the repository
and named by commit; it does not mean it has been exercised on a device unless the row says so.

Line numbers from the original audit are **not** carried forward — most of those files have been
rewritten, and a stale line number is worse than none. Where a finding is still open it points at
the file as it exists today.

## 1. Functionality

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Critical** — Review screen ignored every earlier step; category, description, location, photos and budget were all hardcoded | **Fixed** | `95624e3`. `TaskReviewScreen.kt` binds the draft. The `dummyCategories.first()` fallback that invented "Home Repairs" for an unselected category is gone; it now reads "Not selected". |
| **Critical** — "I want to help" was a dead end; `onRoleSelected("helper")` was unhandled | **Fixed** | `RoleSelectionScreen.kt` emits a `UserRole`, and `FixhoraApp.kt` routes `HELPER` to the worker flow. |
| **High** — Category search field had `value = ""` and an empty `onValueChange` | **Fixed** | `95624e3`. Wired to `TaskViewModel.updateSearchCategoryQuery`. |
| **High** — Location search field was static and non-interactive | **Fixed** | `1ca36da`. Wired, plus real GPS behind it. |
| **High** — Photo upload was purely visual; no picker, mock images could not be removed | **Fixed** | `95624e3`. Android photo picker, and photos are copied into `filesDir/task_photos/` by `TaskPhotoStore` because photo-picker URI grants are **not** persistable — `takePersistableUriPermission` throws on API 33+. |
| **Medium** — "Continue" worked with no category selected | **Fixed** | Validation with an inline error. |
| **Medium** — App bar "Edit" / "Skip" had empty `TODO` handlers | **Fixed** | `95624e3`. Edit returns to the right step instead of resetting the flow. |

## 2. UI / UX

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Medium** — Hindi selection changed a button but not the app's language | **Still open.** The pill is honest about being a preference, but no string is translated. | Full i18n is on the roadmap; see `03_GAPS.md`. |
| **Medium** — "Use my current location" requested no permission and moved no map | **Fixed** | `1ca36da`. Runtime permission, `FusedLocationProviderClient`, `Geocoder`, and a manual-entry fallback when denied. |
| **Low** — No loading states | **Fixed** | Skeletons on the feed, a spinner while photos copy, submit state on Review. |
| **Low** — Interactive areas lacked semantic descriptions | **Fixed** | Batch 7, below. |

## 3. Code quality

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Critical** — No ViewModel; state did not survive between steps | **Fixed** | `TaskViewModel`, `HelperViewModel`, `AuthViewModel`, `UpdateViewModel`, with Room-backed drafts. |
| **High** — Strings hardcoded in Compose files rather than `strings.xml` | **Still open, deliberately.** `strings.xml` holds only a comment explaining that `app_name` is generated per build type. | Extraction is the first step of i18n and belongs with it, not before it. |
| **Medium** — String-based navigation instead of type-safe routes | **Still open.** `Screen` in `FixhoraApp.kt` is a sealed class, so routes are centralised, but they are still strings. | Low value until the graph grows. |
| **Low** — Unused Room and Retrofit dependencies | **Fixed** | `0d1ecd0`. Retrofit, OkHttp, the logging interceptor, Moshi (+ its KSP), the Moshi converter and the Firebase BOM were all removed — nothing imported them. Room is now genuinely used. |

## 4. Backend / API

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Critical** — Retrofit present but no client, no models, no interfaces | **Resolved by decision, not by code.** The app is local-only; Room is the single source of truth. The unused networking stack was removed rather than filled in. | The one network call in the app is the updater's GitHub check, on `HttpURLConnection`. |
| **High** — "Post Task" only navigated; nothing was persisted | **Fixed** | `95624e3`. `submitTask` writes in a transaction and the UI navigates only after it succeeds; a failure is shown and retryable. |

## 5. Database

| Original finding | State | Where |
| :--- | :--- | :--- |
| **High** — Room dependencies present, no entities, DAOs or database; drafts lost on kill | **Fixed** | `f853f40`. `AppDatabase` v3 with `TaskEntity`, `UserEntity`, chat entities, explicit `Migration` objects, and `fallbackToDestructiveMigration` confined to debug — in release it would have silently deleted a user's data. |

## 6. Security

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Medium** — Claimed location was safe but never requested permission | **Fixed** | `1ca36da`. |
| *(not in the original audit)* Passwords | **Fixed** | `f853f40`. PBKDF2-HMAC-SHA256, 100,000 iterations, per-user salt, over `javax.crypto.Mac` — `SecretKeyFactory`'s PBKDF2WithHmacSHA256 needs API 26 and `minSdk` is 24. Covered by `PasswordHasherTest` against published vectors. |
| *(not in the original audit)* Signing key | **Handled** | The release key lives in a GitHub Secret and is generated by the owner via `scripts/make-release-keystore.sh`, so it never passes through a transcript. The debug key is committed on purpose and is not a secret. |

## 7. Performance

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Medium** — `categories` list rebuilt on every recomposition | **Fixed** | `dummyCategories` is a top-level `val` in `CategoriesData.kt`, allocated once. |
| *(not in the original audit)* Seeding race | **Fixed** | `f853f40`. `TaskViewModel` and `HelperViewModel` both seeded on init, so a race could produce a doubled data set — and it ran in release. Seeding now happens once, from `FixhoraApplication`, behind `BuildConfig.DEBUG`. |

## 8. DevOps / deployment

| Original finding | State | Where |
| :--- | :--- | :--- |
| **Medium** — `.env.example` declared no expected configuration | **Fixed** | `0d1ecd0` removed the unused `GEMINI_API_KEY` and the false Gemini capability in `metadata.json`; the file now documents that no key is needed and shows the shape for the first one that is. |
| *(not in the original audit)* Build was broken on a fresh clone | **Fixed** | `0d1ecd0`. `debug` pointed its signing config at `${rootDir}/debug.keystore`, which is gitignored and not in the repository, so `assembleDebug` failed immediately after cloning. |
| *(not in the original audit)* No CI | **Fixed** | `6be91ec`. `.github/workflows/release-apk.yml` runs the tests, builds, verifies the APK is genuinely signed with `apksigner`, and publishes it to a GitHub Release. |
| *(not in the original audit)* Every launcher icon was corrupt | **Fixed** | `837b290`. All 20 PNGs under `res/mipmap-*/` began `ef bf bd 50 4e 47` instead of `89 50 4e 47` — a binary file read as text and written back. `isCrunchPngs = false` is why it shipped silently; aapt copied them through without decoding. |

## Batch 7 — accessibility

Found by reading the code, not by running a scanner; each row names what a user would hit.

| Finding | Fix |
| :--- | :--- |
| The photo remove button was a **20dp** touch target — under half the 48dp minimum | The visible circle is unchanged; the tappable area is now 48dp. `TaskPhotosScreen.kt` |
| That button's icon had `contentDescription = null` and was the **only** content of the control, so a screen reader announced nothing at all | Labelled "Remove photo N". |
| Every thumbnail read "Uploaded photo", so no two could be told apart | Now "Photo N of M". |
| The "Add another photo" card's only content was an unlabelled icon | Labelled. |
| Category cards used `clickable`, so selection was conveyed by border and tint only — invisible to a screen reader | `selectable` with `Role.RadioButton`, which announces selected state. |
| Worker bottom-nav tabs had the same problem | `selectable` with `Role.Tab`. |
| Three text links ("Create New Account", "Sign In", "Resend code") were bare `Text` with `clickable` — about 20dp tall and announced as text, not buttons | Padding moved inside the clickable so the target reaches 48dp, plus `Role.Button`. |
| `ExampleInstrumentedTest` asserted `packageName == "com.example"` | **Deleted.** `applicationId` is `com.fixhora.app`, so this template test had become a failing test nobody was running. `ExampleUnitTest` (asserting 2 + 2 = 4) went with it. |

## Not audited here

Runtime behaviour on a device. Everything above is a code-level finding. What a screen reader
actually announces, and whether the touch targets feel right under a thumb, is confirmed by the
script in `06_MANUAL_TESTS.md` — not by this document.
