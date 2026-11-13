# Manual test checklist (things that need a real phone)

Install the debug build (`./gradlew installDebug`) on at least: one budget Android (3–4 GB RAM,
Redmi/Realme), one mid-range Android, one tablet or foldable.

## Fixed in this audit — please confirm on a phone
- [ ] App icon shows correctly on the home screen (it was corrupted before) — also themed icon on Android 13+
- [ ] No black screens anywhere in Sign in / Mobile / OTP / Profile
- [ ] Phone in **dark mode**: clock/battery icons at the top stay visible; colours not inverted on Xiaomi/Redmi (MIUI/HyperOS)
- [ ] Mobile number: type `+91 98765-43210`, `098765 43210`, paste from Contacts → becomes 10 digits; `5876543210` is refused
- [ ] OTP: type, Backspace, paste a 6-digit code; rotate the phone → digits stay
- [ ] After "Verify" / "Sign In", press Back → app does **not** return to login screens
- [ ] Settings → Display → Font size **largest** + Display size largest: Welcome, Sign in, Role cards, Category tiles — no cut text, buttons reachable by scrolling
- [ ] Keyboard open on Sign in / Create account / Task details → field being typed is visible
- [ ] 3-button navigation and gesture navigation: nothing hidden under the bars
- [ ] Post a task: tap "Post Task" twice quickly → only one task; then "I need help" again → empty form (no old draft)
- [ ] Add 2 photos to a task, force-stop the app, reopen → photos still show
- [ ] Small phone (or largest font): on "What do you need help with?" swipe up → heading scrolls away and all 9 categories are reachable; Continue stays at the bottom
- [ ] Category search still keeps the keyboard open while typing
- [ ] Budget: letters not accepted; Min 2000 / Max 500 shows an error; review shows ₹1,25,000 style
- [ ] Helper → Chat → open a chat → Back closes the chat (does not leave the helper area)
- [ ] Helper → Tasks: reject a task → it shows "Rejected"; search box filters the list
- [ ] Release build (`./gradlew assembleRelease` with your keystore): app opens, post a task, helper sees it (checks code shrinking)
- [ ] Developer options → "Don't keep activities": fill the task form, switch apps, come back → draft restored
- [ ] Tablet / unfolded foldable, landscape and split-screen: usable (will look stretched — UI-10 still open)
- [ ] Android 16 phone: back gesture preview and edge-to-edge look right
- [ ] 16 KB page-size emulator image (Android 15+): app starts

## New login and screens (test mode — before Firebase is set up)
- [ ] Fresh install → Welcome shows English/हिंदी, "Continue with mobile number", "Continue with Google", "Explore without login"
- [ ] Mobile screen: "Pick number from my SIM" shows your SIM numbers (dual-SIM: both) and fills the field
- [ ] OTP screen shows the yellow "Test mode" note; 000000 → "Wrong OTP … 2 tries left"; after 3 wrong → asks to resend
- [ ] 123456 → asks your name (first time only) → role choice → the app remembers the side you chose on next launch
- [ ] Reopen the app → you are still logged in (no login screen)
- [ ] While on the OTP screen, switch to the SMS app, wait, come back (also with "Don't keep activities" on) → number and countdown still there
- [ ] Customer: tap a category on Home → task form opens with it chosen → post → "See my tasks" shows it
- [ ] Switch to helper (Account → Switch) → the task shows "Posted by you"; sample jobs (test builds only) show Send offer / Accept
- [ ] Helper sends an offer → customer side shows it under the task → Choose → helper My jobs shows "Chosen" → Chat works both ways
- [ ] Online switch off → jobs hidden with a message; stays off after restart
- [ ] Back on the first tab → "Press back again to exit"; Back inside a chat closes the chat first
- [ ] Log out → back to Welcome; tasks/offers/chats/photos gone from this phone
- [ ] Delete my account → confirm dialog → back to Welcome, data gone

## After Firebase is set up (docs/FIREBASE_SETUP.md)
- [ ] Test number from Firebase console logs in with its fixed code
- [ ] Real OTP on Jio, Airtel, Vi, BSNL: arrives, is read automatically (Android), resend after 30 s works
- [ ] "Continue with Google" on a **release build from the Play internal testing track** (needs the Play signing SHA-1/SHA-256 in Firebase)
- [ ] Cancel the Google popup → no error shown; phone with no Google account → clear message
- [ ] Delete account right after logging in works; after a long time → "please log in again" message, then works

## After tasks are stored on the server (next step)
- [ ] Real OTP on Jio, Airtel, Vi, BSNL (arrival time, auto-read, resend, wrong OTP ×3)
- [ ] Google sign-in on a **release build from the Play internal testing track** (needs Play App Signing SHA-1/SHA-256 in the console)
- [ ] Same person via OTP and via Google → one account
- [ ] Slow 2G/3G and airplane mode mid-request: friendly retry, no endless spinner
- [ ] Notifications arrive on Xiaomi/Realme/Oppo/Vivo with battery saver on
- [ ] If payments are added: real ₹1 payment + refund, pay then kill the app inside the UPI app, pay twice quickly

## Before Play Store release
- [ ] Decide brand name and **final package name** (permanent) — D4, D5
- [ ] Target API 36 ✓ (already) · 16 KB support ✓ (already)
- [ ] Release keystore created and backed up; enrol in Play App Signing
- [ ] Privacy Policy, Terms, Contact pages live and linked in the app — D10
- [ ] Account deletion: in-app + public web link
- [ ] Data Safety form matches what the app collects (phone, name, location, photos, chat)
- [ ] Billing: real-world services → no Play Billing needed; revisit if digital extras are sold
- [ ] New personal developer account: closed test with 12+ testers for 14 days
- [ ] Remove/replace fake dashboard numbers (batch 2 preview) before any public build
