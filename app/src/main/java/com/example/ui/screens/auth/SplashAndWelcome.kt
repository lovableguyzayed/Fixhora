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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.BrandLogo
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CustomerArtAspect
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import com.example.ui.components.WorkerArtAspect
import com.example.ui.screens.legal.LegalDocument
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
    onGuestClick: () -> Unit,
    onOpenLegal: (LegalDocument) -> Unit = {}
) {
    val colors = FixTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl)
    ) {
        Spacer(modifier = Modifier.height(Spacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(height = 32.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            BrandWordmark(fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
        // The headline and both personas on one yellow card: an app for asking and for helping,
        // said in one glance.
        BentoCard(colors = Bento.Sun, modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            Column(modifier = Modifier.padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.xl)) {
                Text(
                    text = stringResource(R.string.welcome_headline),
                    style = MaterialTheme.typography.displaySmall,
                    color = Bento.Sun.onFill
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Bento.Sun.onFillSecondary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            WelcomeHero(modifier = Modifier.fillMaxWidth().height(220.dp))
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        // What each side of the app is for, before anyone has to pick one.
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            WelcomeRoleTile(
                title = stringResource(R.string.role_customer_title),
                body = stringResource(R.string.role_customer_description),
                colors = Bento.Mint,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            WelcomeRoleTile(
                title = stringResource(R.string.role_worker_title),
                body = stringResource(R.string.role_worker_description),
                colors = Bento.Tangerine,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
        FixButton(text = stringResource(R.string.action_sign_in), onClick = onSignInClick)
        Spacer(modifier = Modifier.height(Spacing.md))
        FixButton(
            text = stringResource(R.string.action_create_account),
            onClick = onCreateAccountClick,
            style = FixButtonStyle.SECONDARY
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        TextButton(
            onClick = onGuestClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)
        ) {
            Text(
                stringResource(R.string.action_continue_as_guest),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(R.string.welcome_terms_notice),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg)
        )
        LegalLinks(onOpenLegal, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

@Composable
private fun WelcomeRoleTile(title: String, body: String, colors: BentoColors, modifier: Modifier = Modifier) {
    BentoCard(colors = colors, modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.onFill)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onFillSecondary)
    }
}

/**
 * Both personas standing together at the foot of the hero card, on a soft circle of the card's
 * own text colour. Sizes are explicit so both figures stay the same height at any width.
 */
@Composable
private fun WelcomeHero(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val figureHeight = maxHeight * 0.95f
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = maxHeight * 0.35f)
                .size(maxHeight * 1.2f)
                .clip(CircleShape)
                .background(Bento.Sun.onFill.copy(alpha = 0.07f))
        )
        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(-(figureHeight * 0.06f)),
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
