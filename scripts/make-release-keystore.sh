#!/usr/bin/env bash
#
# Generates the release signing key for Fixhora and prints the four values to store as GitHub
# Secrets.
#
# WHY THIS MATTERS
#   Android will only install an APK over an existing one when both are signed with the SAME key.
#   Lose this key and you can never update the app again — you would have to publish it under a
#   new application id and every user would have to uninstall and reinstall. Back up the .jks
#   file somewhere safe and private.
#
# Run this on your own machine. The key should never be pasted into a chat, a ticket, or a commit.
#
#   ./scripts/make-release-keystore.sh
#
set -euo pipefail

KEYSTORE_FILE="${1:-fixhora-release.jks}"
KEY_ALIAS="fixhora"
VALIDITY_DAYS=10950 # 30 years; a signing key has to outlive the app.

if [[ -e "$KEYSTORE_FILE" ]]; then
  echo "ERROR: $KEYSTORE_FILE already exists."
  echo "Refusing to overwrite it — overwriting a signing key is unrecoverable."
  echo "Move the old file aside first if you really mean to replace it."
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: keytool not found. It ships with the JDK — install a JDK, or run this from the"
  echo "JDK that Android Studio bundles."
  exit 1
fi

echo "Creating $KEYSTORE_FILE"
echo "You will be asked for a password. Use a strong one and record it; it cannot be recovered."
echo

read -r -s -p "Keystore password: " STORE_PASSWORD
echo
read -r -s -p "Confirm password:  " STORE_PASSWORD_CONFIRM
echo

if [[ "$STORE_PASSWORD" != "$STORE_PASSWORD_CONFIRM" ]]; then
  echo "ERROR: passwords do not match."
  exit 1
fi
if [[ ${#STORE_PASSWORD} -lt 8 ]]; then
  echo "ERROR: use at least 8 characters."
  exit 1
fi

keytool -genkeypair \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$STORE_PASSWORD" \
  -dname "CN=Fixhora, OU=Mobile, O=Fixhora, L=Noida, ST=Uttar Pradesh, C=IN"

echo
echo "Done. Now add these four repository secrets on GitHub:"
echo "  Settings -> Secrets and variables -> Actions -> New repository secret"
echo
echo "  RELEASE_KEYSTORE_BASE64    (the long string printed below)"
echo "  RELEASE_KEYSTORE_PASSWORD  (the password you just chose)"
echo "  RELEASE_KEY_ALIAS          $KEY_ALIAS"
echo "  RELEASE_KEY_PASSWORD       (the same password)"
echo
echo "----- RELEASE_KEYSTORE_BASE64 (copy everything between the markers) -----"
base64 -w 0 "$KEYSTORE_FILE" 2>/dev/null || base64 "$KEYSTORE_FILE"
echo
echo "----- end -----"
echo
echo "Keep $KEYSTORE_FILE backed up privately. It is gitignored and must stay out of the repo."
