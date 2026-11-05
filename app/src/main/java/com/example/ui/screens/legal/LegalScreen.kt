package com.example.ui.screens.legal

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.FixTopBar
import com.example.ui.theme.Bento
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * The two documents sign-up asks the user to agree to, and the help page, which has the same shape.
 *
 * Sign-up required ticking "I agree to the Terms & Privacy Policy" while neither document existed.
 * The text describes what this build actually does — local storage, no server, no SMS, no
 * payments — and is in both languages, because an agreement the user cannot read is not one.
 */
enum class LegalDocument(
  val route: String,
  @StringRes val titleRes: Int,
  val sections: List<Pair<Int, Int>>,
  /** Agreements carry the date they last changed; help answers do not need one. */
  val showsUpdatedDate: Boolean = true,
) {
  TERMS(
    "terms",
    R.string.legal_terms_title,
    listOf(
      R.string.legal_terms_s1_title to R.string.legal_terms_s1_body,
      R.string.legal_terms_s2_title to R.string.legal_terms_s2_body,
      R.string.legal_terms_s3_title to R.string.legal_terms_s3_body,
      R.string.legal_terms_s4_title to R.string.legal_terms_s4_body,
      R.string.legal_terms_s5_title to R.string.legal_terms_s5_body,
      R.string.legal_terms_s6_title to R.string.legal_terms_s6_body,
    ),
  ),
  PRIVACY(
    "privacy",
    R.string.legal_privacy_title,
    listOf(
      R.string.legal_privacy_s1_title to R.string.legal_privacy_s1_body,
      R.string.legal_privacy_s2_title to R.string.legal_privacy_s2_body,
      R.string.legal_privacy_s3_title to R.string.legal_privacy_s3_body,
      R.string.legal_privacy_s4_title to R.string.legal_privacy_s4_body,
      R.string.legal_privacy_s5_title to R.string.legal_privacy_s5_body,
      R.string.legal_privacy_s6_title to R.string.legal_privacy_s6_body,
    ),
  ),
  HELP(
    "help",
    R.string.help_title,
    listOf(
      R.string.help_s1_title to R.string.help_s1_body,
      R.string.help_s2_title to R.string.help_s2_body,
      R.string.help_s3_title to R.string.help_s3_body,
      R.string.help_s4_title to R.string.help_s4_body,
      R.string.help_s5_title to R.string.help_s5_body,
      R.string.help_s6_title to R.string.help_s6_body,
    ),
    showsUpdatedDate = false,
  );

  companion object {
    fun fromRoute(route: String?): LegalDocument = entries.firstOrNull { it.route == route } ?: TERMS
  }
}

@Composable
fun LegalScreen(document: LegalDocument, onBack: () -> Unit) {
  val colors = FixTheme.colors
  Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
    FixTopBar(title = stringResource(document.titleRes), onBack = onBack)
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
    ) {
      if (document.showsUpdatedDate) {
        BentoCard(colors = Bento.Cream, modifier = Modifier.fillMaxWidth()) {
          Text(
            stringResource(R.string.legal_updated),
            style = MaterialTheme.typography.labelMedium,
            color = Bento.Cream.onFillSecondary,
          )
        }
      }
      document.sections.forEachIndexed { index, (title, body) ->
        if (index > 0 || document.showsUpdatedDate) Spacer(Modifier.height(Spacing.xl))
        Text(
          stringResource(title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = colors.textPrimary,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
      }
      Spacer(Modifier.height(Spacing.xxl))
    }
  }
}
