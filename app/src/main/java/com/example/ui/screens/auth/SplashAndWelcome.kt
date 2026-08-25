package com.example.ui.screens.auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FixTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha.value)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = FixTheme.colors.primary, fontStyle = FontStyle.Italic)) {
                        append(stringResource(R.string.brand_fixora))
                    }
                    withStyle(style = SpanStyle(color = FixTheme.colors.accentGraphic, fontStyle = FontStyle.Italic, fontSize = 48.sp)) {
                        append("X")
                    }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FixTheme.colors.primary,
                modifier = Modifier
                    .alpha(alpha.value)
                    .offset(y = (-28).dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 14.sp,
                color = FixTheme.colors.textSecondary,
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
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FixTheme.colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.img_worker), // Assuming worker illustration acts as large welcome image
            contentDescription = stringResource(R.string.cd_welcome_illustration),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = FixTheme.colors.textPrimary)) {
                    append(stringResource(R.string.welcome_title_prefix))
                }
                withStyle(style = SpanStyle(color = FixTheme.colors.primary, fontStyle = FontStyle.Italic)) {
                    append(stringResource(R.string.brand_fixora))
                }
                withStyle(style = SpanStyle(color = FixTheme.colors.accentGraphic, fontStyle = FontStyle.Italic, fontSize = 42.sp)) {
                    append("X")
                }
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FixTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.welcome_subtitle),
            fontSize = 16.sp,
            color = FixTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
        ) {
            Text(stringResource(R.string.action_sign_in), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onCreateAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FixTheme.colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FixTheme.colors.primary)
        ) {
            Text(stringResource(R.string.action_create_account), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onGuestClick) {
            Text(stringResource(R.string.action_continue_as_guest), color = FixTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.welcome_terms_notice),
            fontSize = 12.sp,
            color = FixTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
