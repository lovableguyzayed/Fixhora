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

Later releases install **straight over** the one you have. Do not uninstall first — uninstalling
wipes your account and posted tasks, since everything lives on the device.

This works because every APK from the release workflow is signed with the same key. Android
refuses an update signed with a different key, which is the single most common reason a sideloaded
update fails.

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

Debug builds are signed with your machine's own debug keystore and install as a separate app
(`com.fixhora.app.debug`, labelled "FixoraX (Debug)"), so they never collide with a release build
you have installed.

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
- **No payments, ratings or bidding.**
- Photos are copied at full resolution; five large photos can be tens of megabytes.

---

## Project history

`05_CHANGELOG.md` records each batch of work as `Changed | Reason | Risk`, including the bugs
found and what was deliberately left undone.
