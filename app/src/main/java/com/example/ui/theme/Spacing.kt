package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * A 4dp-based spacing scale.
 *
 * Screens previously picked from a spread of ad-hoc values (6, 12, 28, 48…), which is why nothing
 * lined up between one screen and the next. Named steps make an inconsistency visible in review.
 */
object Spacing {
  /** 4dp — between an icon and its label. */
  val xs = 4.dp

  /** 8dp — inside a chip or badge. */
  val sm = 8.dp

  /** 12dp — between related rows. */
  val md = 12.dp

  /** 16dp — default padding inside a card. */
  val lg = 16.dp

  /** 24dp — screen edge padding, and between sections. */
  val xl = 24.dp

  /** 32dp — between major blocks. */
  val xxl = 32.dp

  /** 48dp — above a page's primary action. */
  val xxxl = 48.dp
}

/** Corner radii, matched to component size so a chip never looks like a card. */
object Radius {
  /** 8dp — badges, small chips. */
  val sm = 8.dp

  /** 12dp — buttons, inputs. */
  val md = 12.dp

  /** 16dp — cards. */
  val lg = 16.dp

  /** 24dp — hero cards and sheets. */
  val xl = 24.dp
}

/** Minimum size for anything tappable, per the Android accessibility guidance. */
val MinTouchTarget = 48.dp
