package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FixTheme

/** Width / height of `img_logo`, cropped to the mark itself. */
private const val LogoAspect = 290f / 384f

/**
 * The location-pin logo.
 *
 * The artwork is cropped to the mark. The original canvas was about 60% transparent padding,
 * which is why the wordmark under it used to be pulled up with `offset(y = (-28).dp)`.
 */
@Composable
fun BrandLogo(modifier: Modifier = Modifier, height: Dp = 72.dp) {
  Image(
    painter = painterResource(id = R.drawable.img_logo),
    contentDescription = stringResource(R.string.cd_logo),
    modifier = modifier.size(width = height * LogoAspect, height = height),
  )
}

/**
 * "FixoraX" in the brand colours. One size for both halves: the larger "X" the screens used to
 * draw changed the line height, and was the other half of the reason for the offset hack.
 */
@Composable
fun BrandWordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 32.sp) {
  val colors = FixTheme.colors
  Text(
    text =
      buildAnnotatedString {
        withStyle(SpanStyle(color = colors.primary)) { append(stringResource(R.string.brand_fixora)) }
        withStyle(SpanStyle(color = colors.accentGraphic)) { append(stringResource(R.string.brand_x)) }
      },
    modifier = modifier,
    fontSize = fontSize,
    lineHeight = fontSize * 1.15f,
    fontWeight = FontWeight.ExtraBold,
    fontStyle = FontStyle.Italic,
    letterSpacing = (-0.5).sp,
  )
}
