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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BrandLogo
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CustomerArtAspect
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import com.example.ui.components.PopOutCircleShape
import com.example.ui.components.WorkerArtAspect
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
            .background(FixTheme.colors.background)
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
            Spacer(modifier = Modifier.height(Spacing.lg))
            BrandWordmark(fontSize = 40.sp)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = FixTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.xxxl)
            )
        }

        LinearProgressIndicator(
            progress = { progress.value },
            color = FixTheme.colors.primary,
            trackColor = FixTheme.colors.primarySurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.xxxl)
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
            .background(FixTheme.colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(height = 32.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            BrandWordmark(fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
        WelcomeHero(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .aspectRatio(1.1f)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.welcome_headline),
            style = MaterialTheme.typography.headlineLarge,
            color = FixTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = FixTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.sm)
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))
        FixButton(text = stringResource(R.string.action_sign_in), onClick = onSignInClick)
        Spacer(modifier = Modifier.height(Spacing.md))
        FixButton(
            text = stringResource(R.string.action_create_account),
            onClick = onCreateAccountClick,
            style = FixButtonStyle.SECONDARY
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        TextButton(onClick = onGuestClick, modifier = Modifier.heightIn(min = MinTouchTarget)) {
            Text(
                stringResource(R.string.action_continue_as_guest),
                style = MaterialTheme.typography.labelLarge,
                color = FixTheme.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(R.string.welcome_terms_notice),
            style = MaterialTheme.typography.bodySmall,
            color = FixTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

/**
 * Both personas standing together in one soft brand-coloured circle.
 *
 * The welcome screen used to show only the worker, small and adrift in a large empty area, for an
 * app that serves two roles equally. Sizes are explicit so both figures stay the same height and
 * fit inside the circle at any width.
 */
@Composable
private fun WelcomeHero(modifier: Modifier = Modifier) {
    val colors = FixTheme.colors
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val diameter = minOf(maxWidth, maxHeight / 1.1f)
        val figureHeight = diameter * 0.8f
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
                    .background(Brush.linearGradient(listOf(colors.primarySurface, colors.accentSurface)))
            )
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(-(diameter * 0.04f)),
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_customer),
                    contentDescription = stringResource(R.string.cd_welcome_illustration),
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
