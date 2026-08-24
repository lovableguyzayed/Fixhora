# Fixhora (FixoraX)

An Android marketplace for local help. Someone with a job to be done posts a task; someone nearby
picks it up.

Kotlin, Jetpack Compose, Material 3, Room. **Local-first** — the database on the device is the
single source of truth, so both roles work on one phone and nothing is synced to a server.

---

## Getting the app

Every push builds a signed APK and publishes it to
[**Releases**](../../releases). Open that page on your phone, download the `.apk`, and tap it.

Android will ask you to allow installing from your browser the first time. That is expected for
any app that does not come from the Play Store.

### Updating

**The app updates itself.** From 1.0.15 onwards it checks the releases page on launch — at most
once every six hours — and offers any newer build in a dialog: *Update now* downloads the APK and
opens the installer. You never have to visit the releases page again.

To check on demand, tap the version line at the bottom of the role-selection screen.

The first time you update this way, Android asks you to allow **"Install unknown apps"** for
FixoraX. The app detects that it does not yet have that permission and takes you straight to the
switch; turn it on, come back, and the download starts on its own. Without that grant a sideloaded
update simply does nothing — no error, no installer — which is the most common reason a manual
update appears to fail.

Later releases install **straight over** the one you have. Do not uninstall first — uninstalling
wipes your account and posted tasks, since everything lives on the device.

This works because every APK from the release workflow is signed with the same key. Android
refuses an update signed with a different key, which is the single most common reason a sideloaded
install is rejected outright.

The updater only ever offers the APK for the channel it is running in: a debug build is offered
`fixhora-<version>-debug.apk`, a release build `fixhora-<version>.apk`. The two have different
application ids and different signing keys, so crossing them would produce a failed install rather
than an update.

If the check itself fails — no connection, GitHub rate-limiting the request — the dialog says
which, and offers the releases page as a fallback. An automatic check that fails stays silent; only
a check you asked for reports back.

### "App not installed" — what it means

| Cause | Fix |
| :-- | :-- |
| You still have the old pre-release build | Its application id was `com.aistudio.fixhora.xyzkpa`; the current one is `com.fixhora.app`. Uninstall the old one once. This is a one-time cost. |
| You are trying to install a locally built debug APK over a release APK | They are separate apps on purpose — debug installs as **FixoraX (Debug)** and the two coexist. If you did force one over the other, uninstall and pick one channel. |
| You are installing an older release over a newer one | Android does not allow downgrades. Uninstall first, or install the newest release. |
| Not enough storage | Free some space; the APK unpacks to several times its download size. |

---

## Building it yourself

```bash
./gradlew test           # unit tests, no emulator needed
./gradlew assembleDebug  # debug APK -> app/build/outputs/apk/debug/
```

Requires JDK 17+ and the Android SDK (Android Studio installs both). Nothing else — no API keys,
no `.env` values, no backend.

Debug builds are signed with `keystore/fixhora-debug.jks`, which is committed on purpose: AGP's
default is your machine's own `~/.android/debug.keystore`, so a debug APK built on one machine
could never update one built on another. A debug key is not a secret — Android's own default is
public, and the Play Store rejects debug-signed builds regardless.

They install as a separate app (`com.fixhora.app.debug`, labelled "FixoraX (Debug)"), so they never
collide with a release build you have installed.

### Versioning

`versionCode` is the number of commits on the branch, and `versionName` is `1.0.<versionCode>`.
Nothing to bump by hand — the version rises with every commit, which is what makes each build a
valid upgrade of the last. The running version is shown at the bottom of the role-selection
screen.

CI checks out with `fetch-depth: 0` for this reason. A shallow clone would count fewer commits,
produce a *lower* version than the previous build, and Android would refuse to install it.

To pin a build: `./gradlew assembleRelease -PversionCode=250`.

---

## Release signing

The release key is **not** in this repository. It lives in GitHub Actions secrets.

To set it up (once):

```bash
./scripts/make-release-keystore.sh
```

It generates `fixhora-release.jks` and prints the four values to add under
**Settings → Secrets and variables → Actions**:

| Secret | What |
| :-- | :-- |
| `RELEASE_KEYSTORE_BASE64` | the keystore, base64-encoded |
| `RELEASE_KEYSTORE_PASSWORD` | the password you chose |
| `RELEASE_KEY_ALIAS` | `fixhora` |
| `RELEASE_KEY_PASSWORD` | the same password |

**Back up the `.jks` file privately.** If it is lost, no future build can update an installed
app — you would have to ship under a new application id and every user would reinstall from
scratch. If it leaks, someone else can sign an APK that Android treats as yours.

---

## Architecture

```
MainActivity ── MyApplicationTheme (light/dark from system + stored preference)
     └── FixhoraApp  (root NavHost)
          ├── auth/     Splash → Welcome → SignIn / SignUp / Mobile OTP / Forgot password
          │             → Profile setup → Permissions → Role selection
          ├── TaskFlowContainer   (customer)  Category → Location → Photos → Review → Posted
          └── HelperFlowContainer (worker)    Home | Map | Chat | Tasks
                                   │
   ViewModels ── AuthViewModel, TaskViewModel, HelperViewModel
        │
   Repositories ── UserRepository, TaskRepository, ChatRepository
        │
   Room (fixhora-database) ── users, tasks, chat_messages
   DataStore ── session: signed-in user, active role, theme, language
```

| Directory | Holds |
| :-- | :-- |
| `data/room/` | Entities, DAOs, `AppDatabase` and its migrations |
| `data/repository/` | The only code that talks to DAOs |
| `data/session/` | Who is signed in, and their preferences |
| `data/security/` | `PasswordHasher` — salted PBKDF2-HMAC-SHA256 |
| `data/location/` | Real GPS and geocoding |
| `data/media/` | `TaskPhotoStore`, which copies attached photos into app storage |
| `ui/theme/` | `Palette` (raw values), `FixColors`, typography, spacing |
| `ui/components/` | `FixButton`, `FixCard`, `StatusBadge`, `EmptyState`, `SkeletonBox` |
| `ui/screens/` | Compose screens, grouped by flow |

---

## What this app does not do yet

Stated plainly, because the UI does not pretend otherwise:

- **No backend.** Two phones cannot see each other's tasks. Both roles are explorable on one device.
- **OTP is demo-only.** There is no SMS gateway; the code is generated on-device and shown on
  screen, behind a visible "Demo mode" notice.
- **The map is a static graphic**, labelled as a preview. Location itself is real — GPS and
  reverse geocoding both work.
- **No payments, ratings or bidding.** These were removed from the UI rather than faked — a 4.9★
  rating with nothing behind it is worse than no rating.
- **Hindi is a stored preference only.** No string is translated yet; the strings are still
  hardcoded in the Compose files.
- **No distance on job cards.** That needs the worker's own position tracked, which it is not, so
  the address is shown instead.
- Photos are copied at full resolution; five large photos can be tens of megabytes.

`03_GAPS.md` goes further: what is missing and buildable today, what is genuinely blocked on a
backend, and what is deliberately not being done.

---

## Testing

```bash
./gradlew test   # 77 unit tests across 12 files; CI runs these on every push
```

Those cover the logic that has no Android dependency — password hashing, validators, task status
mapping, tab and search filtering, worker formatting, WCAG contrast, and the update checker. They
run on the JVM in seconds, with no emulator.

Nothing automated exercises a Compose screen, a Room migration, or a real device. That gap is
covered by **`06_MANUAL_TESTS.md`** — a numbered script for both roles, happy and failure paths,
including the install-and-update sequence that this project has broken and fixed more than once.
Run it against a release before handing the APK to anyone.

---

## Project history

`05_CHANGELOG.md` records each batch of work as `Changed | Reason | Risk`, including the bugs
found and what was deliberately left undone. `02_AUDIT.md` tracks every original audit finding to
its current state.
