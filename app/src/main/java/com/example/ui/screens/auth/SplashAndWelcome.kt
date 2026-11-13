package com.example.ui.screens.auth

import androidx.compose.ui.res.stringResource

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import com.example.R
import com.example.ui.theme.*

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
        // No artificial waiting: continue as soon as the logo has appeared.
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = stringResource(R.string.splashandwelcome_fixorax_logo),
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha.value)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = BluePrimary, fontStyle = FontStyle.Italic)) {
                        append("Fixora")
                    }
                    withStyle(style = SpanStyle(color = OrangeSecondary, fontStyle = FontStyle.Italic, fontSize = 48.sp)) {
                        append("X")
                    }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary,
                modifier = Modifier
                    .alpha(alpha.value)
                    .offset(y = (-28).dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splashandwelcome_connecting_skilled_workers_with_people_who),
                fontSize = 14.sp,
                color = SecondaryGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .alpha(alpha.value)
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    googleBusy: Boolean,
    errorText: String?,
    onMobileClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onGuestClick: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hindiSoon = stringResource(R.string.auth_hindi_coming_soon)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
    // The picture uses the space left over on tall phones and shrinks on small ones;
    // the screen scrolls when text is large, so buttons are never pushed off-screen.
    val imageHeight = (maxHeight - 500.dp).coerceIn(110.dp, 260.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Language choice up front. Hindi text is being prepared (needs approved wording).
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("English") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEEF4FF), selectedLabelColor = BluePrimary))
            FilterChip(selected = false, onClick = {
                android.widget.Toast.makeText(context, hindiSoon, android.widget.Toast.LENGTH_SHORT).show()
            }, label = { Text("हिंदी") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = R.drawable.img_worker),
            contentDescription = null,
            modifier = Modifier.height(imageHeight).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = DarkNavy)) { append(stringResource(R.string.auth_welcome_to)) }
                withStyle(style = SpanStyle(color = BluePrimary, fontStyle = FontStyle.Italic)) { append("Fixora") }
                withStyle(style = SpanStyle(color = OrangeSecondary, fontStyle = FontStyle.Italic, fontSize = 34.sp)) { append("X") }
            },
            fontSize = 28.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.splashandwelcome_find_trusted_local_workers_or_offer),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = SecondaryGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onMobileClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.auth_continue_mobile), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGoogleClick,
            enabled = !googleBusy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
        ) {
            if (googleBusy) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Image(painterResource(R.drawable.ic_google_g), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.login_continue_google), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
        if (errorText != null) {
            Spacer(Modifier.height(10.dp))
            Text(errorText, color = Color(0xFFC62828), fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        TextButton(onClick = onGuestClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.auth_explore_without_login), color = SecondaryGrey, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
        PolicyLine()
    }
    }
}

/** "By continuing, you agree to our Terms and Privacy Policy" — links only when the pages exist. */
@Composable
fun PolicyLine() {
    val uri = androidx.compose.ui.platform.LocalUriHandler.current
    val terms = com.example.BuildConfig.TERMS_URL
    val privacy = com.example.BuildConfig.PRIVACY_URL
    Text(
        text = stringResource(R.string.auth_policy_line),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = SecondaryGrey,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    if (terms.isNotEmpty() || privacy.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.Center) {
            if (terms.isNotEmpty()) TextButton(onClick = { uri.openUri(terms) }) { Text(stringResource(R.string.account_terms), fontSize = 13.sp) }
            if (privacy.isNotEmpty()) TextButton(onClick = { uri.openUri(privacy) }) { Text(stringResource(R.string.account_privacy), fontSize = 13.sp) }
        }
    }
}
