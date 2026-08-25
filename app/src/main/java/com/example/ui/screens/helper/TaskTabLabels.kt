package com.example.ui.screens.helper

import androidx.annotation.StringRes
import com.example.R

/**
 * What each tab is called.
 *
 * Deliberately not a property on [TaskTab] itself: that enum carries the status mapping, which is
 * the part with a bug history and the part covered by `TaskFiltersTest`, and it is kept free of
 * Android imports so those tests run on a plain JVM. Putting `R.string` on the enum would drag the
 * generated resource class into it for nothing more than a label.
 */
@get:StringRes
val TaskTab.labelRes: Int
  get() =
    when (this) {
      TaskTab.ALL -> R.string.tab_all
      TaskTab.NEW -> R.string.tab_new
      TaskTab.ACCEPTED -> R.string.tab_accepted
      TaskTab.IN_PROGRESS -> R.string.tab_in_progress
      TaskTab.COMPLETED -> R.string.tab_completed
      TaskTab.DECLINED -> R.string.tab_declined
    }
