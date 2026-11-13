# Turning on real login (Firebase) — steps for the owner

Until you finish these steps, the app runs in **test mode**:
- no SMS is sent, and the OTP is always `123456`;
- "Continue with Google" says it is not ready yet;
- a yellow "Test mode" note shows on the login screens and on the Account screen.

Nothing in the code needs to change when you switch Firebase on. You add one file and rebuild.

You need a Google account and about 30–45 minutes. The app's package name is **`in.fixorax.app`** (decided; the Play Store never lets you change it).

## 1. Create the Firebase project
1. Go to https://console.firebase.google.com and click **Create a project**. Name it **FixoraX**.
2. Google Analytics: you can say **No** for now (add it later with crash reporting, D11).
3. In the project, click the **Android** icon (**Add app**).
   - **Android package name:** `in.fixorax.app`
   - **App nickname:** FixoraX.
   - **SHA-1:** leave it empty for now; it is added in step 3.
4. Download **google-services.json** and put it in the project's `app/` folder (next to `app/build.gradle.kts`).
   - It is not a secret password, but it belongs to your project. Ask your developer before committing it to a public repository.

## 2. Switch on the two login methods
In the Firebase console, open **Build → Authentication → Sign-in method**:
1. **Phone:** Enable. Under **Phone numbers for testing**, add one number with a fixed code, for example `+91 99999 00001` / `123456`. Use it during testing, so SMS limits are not used up.
2. **Google:** Enable, choose a support email, and Save.

Under **Authentication → Settings → User account linking**, keep **"Link accounts that use the same email"**. That way one person with the same Google email gets one account.

## 3. Add the app's fingerprints (the most common reason Google/OTP login fails)
Firebase only trusts builds whose signing "fingerprints" (SHA-1 and SHA-256) it knows. You need **three sets**:

| Build | Where to get the SHA-1 and SHA-256 |
|---|---|
| Debug (your developer's phone) | Run `./gradlew signingReport` and look under **debug** |
| Release (your upload key) | Run `./gradlew signingReport` with `KEYSTORE_PATH`, `STORE_PASSWORD` and `KEY_PASSWORD` set, and look under **release** |
| Play Store (Google re-signs your app) | Play Console → your app → **Test and release → App integrity → App signing key certificate** |

Add all of them in Firebase → **Project settings** (gear icon) → your Android app → **Add fingerprint**. Then **download google-services.json again** and replace the old one.

If the Play Store fingerprint is missing, Google sign-in and automatic SMS reading work on the developer's phone but **fail for users who install from the Play Store**. This is the classic bug.

## 4. Play Integrity (stops fake OTP requests that cost money)
Firebase phone login checks that requests come from your real app.
1. In Google Cloud Console (same project), enable **Play Integrity API**.
2. In the Play Console, link the Cloud project under **App integrity**.

Without this, users may sometimes see a short web check (reCAPTCHA) before the OTP is sent. That still works, but it is slower.

## 5. Build and test
1. Rebuild the app. When `app/google-services.json` is present, the app switches to real login automatically.
2. Test with the **test number** from step 2 first. Then test with real SIMs (Jio, Airtel, Vi, BSNL) on a release build from the Play **internal testing** track.

## 6. Costs and limits (check the current Firebase pricing page)
- Phone OTP SMS is a paid Firebase feature, billed per SMS, and needs the **Blaze (pay as you go)** plan. Set a **budget alert** in Google Cloud billing.
- If SMS costs grow, you can later switch to an Indian SMS provider such as MSG91. That needs TRAI DLT registration of your sender ID and message template, and the app's SMS hash.

## 7. Help and policy links in the app
Add these lines to `gradle.properties` (project root), then rebuild. Each row appears in the Account screen once it has a value:
```
fixorax.supportWhatsApp=91XXXXXXXXXX
fixorax.supportPhone=+91XXXXXXXXXX
fixorax.privacyUrl=https://your-site/privacy
fixorax.termsUrl=https://your-site/terms
fixorax.faqUrl=https://your-site/faq
fixorax.accountDeletionUrl=https://your-site/delete-account
```
The Play Store requires a **privacy policy link** and a **web page where people can ask to delete their account** without installing the app.

## 8. Switch on the shared database (Cloud Firestore)
Once this is on, tasks, offers and chats are shared between phones: a customer posts on one phone, and helpers see it on theirs.
1. Firebase console → **Build → Firestore Database → Create database**. Location: **asia-south1 (Mumbai)**, so it is close to your users and data stays in India. Start in **production mode**.
2. Open the **Rules** tab and replace everything with the contents of `firebase/firestore.rules` from this project. Click **Publish**.
   - These rules decide who may read and change what. Example: only the customer and the chosen helper can read their chat.
   - They were tested with 13 checks on Google's emulator (`cd firebase/rules-tests && npm test`).
3. Rebuild the app. Nothing else is needed: with `google-services.json` present, the app uses the shared database automatically.

## What is not shared yet
- **Task photos** stay on the customer's phone; helpers don't see them yet. Sharing them needs Firebase Storage (a small next step).
- **"Jobs near you"** shows the newest 200 open tasks from everywhere, not by distance. Distance needs real location (decision D7).
- **Notifications** (for example "You got a new offer") are not built yet. They need Firebase Cloud Messaging and a small server function.
