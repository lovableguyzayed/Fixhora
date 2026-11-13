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
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
    // The picture uses the space left over on tall phones and shrinks on small ones;
    // the screen scrolls when text is large, so buttons are never pushed off-screen.
    val imageHeight = (maxHeight - 430.dp).coerceIn(120.dp, 420.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.img_worker),
            contentDescription = null,
            modifier = Modifier
                .height(imageHeight)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = DarkNavy)) {
                    append("Welcome to ")
                }
                withStyle(style = SpanStyle(color = BluePrimary, fontStyle = FontStyle.Italic)) {
                    append("Fixora")
                }
                withStyle(style = SpanStyle(color = OrangeSecondary, fontStyle = FontStyle.Italic, fontSize = 42.sp)) {
                    append("X")
                }
            },
            fontSize = 32.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.splashandwelcome_find_trusted_local_workers_or_offer),
            fontSize = 16.sp,
            color = SecondaryGrey,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text(stringResource(R.string.splashandwelcome_sign_in), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onCreateAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
        ) {
            Text(stringResource(R.string.splashandwelcome_create_account), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onGuestClick) {
            Text(stringResource(R.string.splashandwelcome_continue_as_guest), color = SecondaryGrey, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.splashandwelcome_by_continuing_you_agree_to_our),
            fontSize = 12.sp,
            color = SecondaryGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
    }
}
