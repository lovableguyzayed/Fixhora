package com.example.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BrandLogo
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CustomerArtAspect
import com.example.ui.components.WorkerArtAspect
import com.example.ui.components.PopOutCircleShape
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.Spacing
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        delay(1200)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(progress.value)
                .scale(0.92f + 0.08f * progress.value)
        ) {
            BrandLogo(height = 104.dp)
            Spacer(modifier = Modifier.height(Spacing.md))
            BrandWordmark(fontSize = 40.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Connecting skilled workers with people who need help.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        LinearProgressIndicator(
            progress = { progress.value },
            color = BluePrimary,
            trackColor = BlueContainer,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(96.dp)
                .height(4.dp)
                .clip(CircleShape)
        )
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
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(height = 32.dp)
            Spacer(modifier = Modifier.width(Spacing.xs))
            BrandWordmark(fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        WelcomeHero(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .aspectRatio(1.1f)
        )
        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "Get help. Give help.",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Find trusted local workers or offer your skills to people nearby.",
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xs)
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
        PrimaryButton(text = "Sign In", onClick = onSignInClick)
        Spacer(modifier = Modifier.height(Spacing.sm))
        SecondaryButton(text = "Create Account", onClick = onCreateAccountClick)
        Spacer(modifier = Modifier.height(Spacing.xs))
        TextButton(onClick = onGuestClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("Continue as Guest", color = SecondaryGrey, style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = buildAnnotatedString {
                append("By continuing, you agree to our ")
                withStyle(SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) { append("Terms of Service") }
                append(" and ")
                withStyle(SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) { append("Privacy Policy") }
                append(".")
            },
            style = MaterialTheme.typography.bodySmall,
            color = HintGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

/** Both personas standing together inside one soft brand-coloured circle. */
@Composable
private fun WelcomeHero(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val diameter = minOf(maxWidth, maxHeight / 1.1f)
        Box(
            modifier = Modifier
                .size(width = diameter, height = diameter * 1.1f)
                .clip(PopOutCircleShape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(diameter)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BlueContainer, OrangeContainer)))
            )
            // Explicit sizes keep both figures the same height and inside the circle.
            val figureHeight = diameter * 0.8f
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(-(diameter * 0.04f)),
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_customer),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = figureHeight * CustomerArtAspect, height = figureHeight)
                )
                Image(
                    painter = painterResource(id = R.drawable.img_worker),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = figureHeight * WorkerArtAspect, height = figureHeight)
                )
            }
        }
    }
}
