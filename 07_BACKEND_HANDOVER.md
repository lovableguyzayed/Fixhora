# Backend handover: sync, real OTP, push notifications

This is phase D of the design review. It is a plan and a checklist, not code. All three features
need accounts, keys and a billing method that belong to the app's owner. Code written without them
cannot be run or tested, so it would ship broken. The app stays honest about this in the meantime:
the OTP screen shows the code on screen, the privacy policy says nothing leaves the phone, and
nothing promises a notification.

## What each feature needs

| Feature | What it does for users | What it needs |
| :--- | :--- | :--- |
| **Sync across phones** | A customer on one phone and a helper on another see the same task. Today a task exists only on the phone it was posted from, and reinstalling loses everything. | A server database, and accounts that live on the server. |
| **Real OTP by SMS** | Sign-in proves the person owns the number. Today the code is generated and shown on screen. | An SMS sender. In India, commercial SMS through your own provider needs DLT registration (sender ID and message templates). |
| **Push notifications** | "A job near you", "Your job was accepted", "New message", "The helper marked it done", even with the app closed. | Firebase Cloud Messaging, plus server code that sends a message when something happens. |

## Recommended: Firebase

Firebase fits a small Android team best: one console, a free tier, and SDKs this app can use
directly.

- **Firebase Authentication (phone)** for real OTP. Firebase sends the SMS itself, so you do not
  need your own SMS provider or your own DLT registration. Check the current Firebase docs to
  confirm this for India before relying on it.
- **Cloud Firestore** for users, tasks, chat and ratings. Choose the Mumbai region
  (`asia-south1`).
- **Cloud Storage** for task photos.
- **Cloud Messaging** for push, and **Cloud Functions** to send it.

Phone-auth SMS beyond the free allowance, and Cloud Functions, need the pay-as-you-go **Blaze**
plan. Set a budget alert on the day you enable it. Check current prices on the Firebase pricing
page; this document does not quote them because they change.

## Owner checklist: what only you can do

1. Create a Firebase project.
2. Add two Android apps to it: `com.fixhora.app` (release) and `com.fixhora.app.debug` (the
   build CI publishes today).
3. Add the SHA-1 and SHA-256 fingerprints of the debug keystore (`keystore/fixhora-debug.jks`), and of the release
   key once `scripts/make-release-keystore.sh` has been run. Phone auth will not work without them.
4. Turn on Phone sign-in, and add two or three test phone numbers with fixed codes for
   development.
5. Create the Firestore database in `asia-south1`.
6. Upgrade to Blaze and set a budget alert.
7. Download `google-services.json`. Add it to the repository's GitHub secrets as base64 (for
   example `GOOGLE_SERVICES_JSON_BASE64`) so CI can write it before building. It identifies the
   project rather than unlocking it, but it does not need to be in a public repository.
8. Decide on the legal side. Once data leaves the phone, the Privacy Policy and Terms in the app
   must be rewritten at the same time. India's Digital Personal Data Protection Act 2023 applies:
   say what is collected and why, get consent, and let people delete their data. The in-app delete
   account already exists and would then have to delete the server copy too.

Once these are done, share the project ID and confirm the secret is set. Development can then
start.

## What changes in the app, in order

Each step can ship on its own.

1. **Sign-in.** Replace the on-screen demo code (`generateDemoOtp` in `MobileLoginFlow.kt`) with
   Firebase phone auth. Passwords can stay as a second way in, or be dropped, because a verified
   number is the identity. Existing local accounts are matched by mobile number on first sign-in.
2. **Data.** Repositories get a remote source, and Room becomes the offline cache, so the app keeps
   working without network. Task ids are local integers today, so tasks need a server id column
   (schema v5, nullable, with a migration test like v4's).
3. **Address privacy.** House number and landmark move to a separate document that only the task's
   owner and its accepted helper can read. Firestore security rules enforce this, so the server
   holds to the promise the app makes today.
4. **Ratings.** A rating is written only by the task's owner, only once, and only after the helper
   marks the job done. Each helper's average is calculated on the server so it cannot be edited.
   The app already shows real averages from on-device data; this makes them hold across phones.
5. **Push.**
   - Each device stores its FCM token on its user record.
   - Cloud Functions send on four events: a task is posted (to helpers whose skills match and who
     are within the radius; this needs a geohash on each task), a job is accepted, a message
     arrives, and a job is marked done.
   - On Android 13 and later, the notification permission request returns. Only then does the
     "we'll let you know" wording come back.

## Alternatives

| Option | Trade-off |
| :--- | :--- |
| **Supabase** (Postgres) | Plain SQL and row-level security, but phone OTP goes through an SMS provider you sign up for yourself (Twilio, MessageBird and others), with the DLT work that brings in India. Push still needs FCM. |
| **Your own server + MSG91 / 2Factor** | Most control and lowest SMS price at volume, but you run the server, the database backups and the security, and DLT registration is yours to do. |

For a first release with real users, Firebase involves the least work.
