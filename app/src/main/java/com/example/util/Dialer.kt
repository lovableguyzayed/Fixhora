package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the phone's dialler with an Indian mobile number filled in. ACTION_DIAL rather than
 * ACTION_CALL: the user still presses call themselves, and the app needs no phone permission.
 * A phone with no dialler (some tablets) makes this a no-op rather than a crash.
 */
fun Context.dialMobile(normalizedMobile: String) {
  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$normalizedMobile")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  try {
    startActivity(intent)
  } catch (e: ActivityNotFoundException) {
    // Nothing to hand the number to.
  }
}
