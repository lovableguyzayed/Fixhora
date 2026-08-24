# Manual test script

Everything in this file is a check that **cannot** be automated here. The unit suite (77 tests,
run by CI on every push) covers logic with no Android dependency; nothing in the repository
exercises a Compose screen, a Room migration, or a real device. That is what this script is for.

Run it on one physical device. 73 checks. Record Pass/Fail per row — a row left blank is not a pass.

**Build under test:** `v1.0.___`  **Device / Android version:** ____________  **Date:** ________

---

## A. Install and update

The point of this section is the one thing that has been broken longest: a newer APK replacing an
older one without being refused.

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| A1 | If a build with the old id `com.aistudio.fixhora.xyzkpa` is installed, uninstall it | Gone. This is a **one-time** cost — the application id changed to `com.fixhora.app`, and Android treats a different id as a different app | |
| A2 | Download the current release APK on the phone and tap it | Android asks to allow installs from the browser; after allowing, it installs | |
| A3 | Open the app, reach the role screen | Footer reads `v1.0.<n>-debug (<n>)` matching the release you installed | |
| A4 | Check the launcher icon on the home screen | The FixoraX icon, not a default Android silhouette | |
| A5 | Tap the version footer | An update dialog appears — either "You are up to date", or an update offer | |
| A6 | Have a newer release published, then tap the footer again | "Update available — Version 1.0.<n+1>" with a download size | |
| A7 | Tap **Update now** on a device that has never allowed this app to install packages | "One permission needed" → **Open settings** → Android's "Install unknown apps" for FixoraX | |
| A8 | Turn the switch on and come back | The download starts on its own, with a progress bar | |
| A9 | Let it finish | The system installer opens by itself | |
| A10 | Confirm the install | App reopens; footer shows the **higher** version | |
| A11 | **After updating, sign in state and posted tasks** | Still there. An update must not wipe data — only an uninstall does | |
| A12 | Turn airplane mode on and tap the version footer | "No internet connection", with **Open releases** offered — not a silent failure | |

## B. Accounts (customer)

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| B1 | Welcome → Create New Account, submit with every field empty | Inline errors per field. **No navigation** | |
| B2 | Enter a 9-digit mobile | "Enter a 10-digit mobile number" | |
| B3 | Enter mismatched passwords | Mismatch error on the confirm field | |
| B4 | Leave the terms checkbox unticked and submit | Blocked, with the reason shown | |
| B5 | Fill everything correctly and submit | Profile setup opens | |
| B6 | Complete profile → permissions screen → **Deny** location | App continues. It does not nag or dead-end | |
| B7 | Sign out, sign in with the **wrong** password | "Incorrect mobile number or password". Still on the sign-in screen | |
| B8 | Sign in correctly | Role selection, greeting your name | |
| B9 | Press **Back** here | App exits. It must **not** return to the sign-in form | |
| B10 | Force-stop the app and reopen | Straight to role selection, still signed in — no splash-to-welcome bounce | |

## C. Posting a task (customer)

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| C1 | "I need help" → press Continue with no category chosen | Blocked with an error | |
| C2 | Type in category search | The grid filters as you type | |
| C3 | Pick a category with TalkBack on | Announced as **selected** — not just "button" | |
| C4 | Location step → toggle "Use my current location" **after granting** permission | Your real area appears, not a fixed "Sector 62, Noida" | |
| C5 | Deny the permission instead | Falls back to manual entry with an explanation | |
| C6 | Tap the "Within 5 km" card | A distance chooser opens and the value actually changes | |
| C7 | Add 2–3 photos from the gallery | Thumbnails appear | |
| C8 | **Force-stop the app, reopen, return to the photos step** | Photos still load. They are copied into app storage, not held as picker URIs — this is the check that they survive process death | |
| C9 | Tap a photo's **remove** button | Comfortable to hit on the first try (it is a 48dp target now, not 20dp) and the photo disappears | |
| C10 | With TalkBack on, focus the thumbnails and the remove button | Thumbnails read "Photo 1 of 3", "Photo 2 of 3"…; remove reads "Remove photo 1". Neither is silent | |
| C11 | Review screen | Category, address, photos and budget all match what you entered. Nothing says "Home Repairs" unless you chose it | |
| C12 | Skip the budget entirely and review | "No budget set" — not a fabricated range | |
| C13 | Press Back mid-flow | A discard confirmation, not a silent loss | |
| C14 | Post the task | Navigates **after** the write succeeds | |
| C15 | Type a title, then immediately post, then reopen the category step | No leftover draft from the debounced autosave | |

## C2. My tasks (customer)

The section this app went longest without: seeing what you posted.

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| C2.1 | After posting, tap **View my tasks** on the success screen | Lands on My tasks with the task you just posted at the top | |
| C2.2 | Read the card | Real title, "Waiting for a helper", the time you posted, your address and budget | |
| C2.3 | Switch to a fresh account with no tasks | Empty state offering "Post a task" — not a blank screen | |
| C2.4 | Open the task | Detail with the status, a plain-language explanation, and your description | |
| C2.5 | Before any worker accepts it | "Messaging opens once a helper takes this task on." **No** message box | |
| C2.6 | Tap **Cancel this task** | A confirmation dialog first. Cancelling is not one tap | |
| C2.7 | Confirm | Status becomes "Cancelled". The task stays in the list — it is history, not deleted | |
| C2.8 | Post another, switch to the worker role, **accept** it, come back to My tasks | Status now "Helper assigned", and the cancel button is **gone** | |
| C2.9 | Open it | Message box is available | |
| C2.10 | Send a message | Appears on the right, with a timestamp | |
| C2.11 | Switch to the worker role and open that chat | The same message appears on the **left** — one conversation, two sides | |
| C2.12 | Reply as the worker, return to the customer detail | The reply is there, on the left | |
| C2.13 | With the detail open, press **system back** | Returns to My tasks. It must **not** exit the whole customer flow | |
| C2.14 | Have the worker mark it Done while the customer detail is open | Status updates on screen without leaving and re-entering | |
| C2.15 | Force-stop and reopen | Tasks, statuses and messages all still there | |

## D. Worker side

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| D1 | Role screen → "I want to help" | The task you just posted is in the feed | |
| D2 | Read a job card | Real poster name (or "Demo request"), a real age like "3 mins ago", the address you entered, the real budget. No 4.8★, no "2.5 km away" | |
| D3 | Summary tiles at the top | Counts match what is actually in the feed | |
| D4 | Search the Tasks tab | Results filter by title, details, location and category | |
| D5 | Open every tab: New, Accepted, In Progress, Completed, Declined | **Each populates when it has matching tasks.** "In Progress" and "Cancelled" used to be permanently empty | |
| D6 | Accept a job, then Start, then Done | It moves tab by tab, and the status survives a restart | |
| D7 | Tap **Chat** on a job card | Opens that conversation on the Chat tab | |
| D8 | Read the conversation before typing anything | Empty state. **No** fabricated "Hello! I saw your job request…" attributed to the customer | |
| D9 | Send a message, force-stop, reopen | Message is there, with a real timestamp | |
| D10 | Tap **Navigate** | A maps app opens at the location, or nothing happens if none is installed — never a crash | |
| D11 | Bottom nav with TalkBack on | The active tab announces as **selected** | |
| D12 | Look for Place Bid, a notification bell, a "3" chat badge, an online toggle | **None of them exist.** They were removed rather than left dead | |

## E. Theme and contrast

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| E1 | Switch the system to dark mode with the app open | Every screen follows. No white card with white text, no invisible progress ring | |
| E2 | Walk all screens in dark mode | No hardcoded light-mode colour survives | |
| E3 | Set system font size to the largest step | Text scales; nothing critical is clipped or overlapped | |
| E4 | The update dialog in both themes | Readable in both | |

## F. Failure paths

| # | Step | Expected | P/F |
| :-- | :--- | :--- | :-- |
| F1 | Airplane mode, then post a task | Works. The app is local-only — posting needs no network | |
| F2 | Airplane mode, then tap the version footer | Reports no connection; the rest of the app is unaffected | |
| F3 | Revoke location permission in system settings while the app runs, then use the location step | Manual entry fallback, no crash | |
| F4 | Fill storage, then try an update download | Reports the download failed and offers the releases page | |
| F5 | Rotate the device on each screen | No crash, no lost input | |

---

## Known limitations — expected, not bugs

Do not file these:

- **No backend.** Accounts and tasks live only on this device. A second phone sees nothing, and an uninstall loses everything.
- **OTP is demo-only.** There is no SMS gateway; debug builds show the code on screen and the app says so.
- **The map is not a map.** `WorkerMapScreen` browses by category and states plainly that no maps SDK is integrated.
- **No distance on job cards.** Showing "2.5 km away" needs the worker's own position tracked, which it is not. The address is shown instead.
- **No ratings, bidding or payments.** Removed rather than faked.
- **Hindi is a stored preference only.** No string is translated yet.
- **Release channel not enabled.** Only debug-signed APKs are published until the four signing secrets exist.
