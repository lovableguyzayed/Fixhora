package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.session.AppLanguage
import com.example.data.session.UserRole
import com.example.ui.components.BentoCard
import com.example.ui.components.BrandLogo
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CircleIconButton
import com.example.ui.components.CustomerArtAspect
import com.example.ui.components.PopOutCircleShape
import com.example.ui.components.WorkerArtAspect
import com.example.ui.theme.*

/**
 * Role choice, laid out to the onboarding design spec: two equal cards side by side, each with
 * its persona, a title, a line of description and a 48dp round action; then the language choice
 * as two 48dp options. Spacing follows the spec (24dp gutter, 16dp between the cards, 32dp
 * between blocks).
 */
@Composable
fun RoleSelectionScreen(
    signedInName: String?,
    onRoleSelected: (UserRole) -> Unit,
    onOpenSettings: () -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    onLanguageChange: (AppLanguage) -> Unit = {},
) {
    val colors = FixTheme.colors

    Scaffold(containerColor = colors.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand and question, over a faint skyline that fades into the page.
            Box(modifier = Modifier.fillMaxWidth()) {
                SkylineBackground(modifier = Modifier.matchParentSize())
                // Profile, theme, help, the terms, the update check and signing out all live in
                // settings now, instead of being stacked under the role choice.
                CircleIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    onClick = onOpenSettings,
                    container = colors.surface,
                    content = colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = Spacing.md, end = Spacing.lg)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xxl))
                    BrandLogo(height = 64.dp)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    BrandWordmark(fontSize = 30.sp)
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // Devanagari sets taller than Latin at the same point size, so the heading gets a
                    // slightly smaller size and tighter leading in Hindi. That is typography, not
                    // translation, which is why it stays in code while the words live in resources.
                    val isHindi = language == AppLanguage.HINDI
                    Text(
                        text = stringResource(R.string.role_heading),
                        fontSize = if (isHindi) 28.sp else 30.sp,
                        lineHeight = if (isHindi) 36.sp else 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = stringResource(R.string.role_subheading),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxl))
                }
            }

            // Equal widths, and equal heights whichever language makes one description longer.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                RoleCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = stringResource(R.string.role_customer_title),
                    description = stringResource(R.string.role_customer_description),
                    bento = Bento.Sun,
                    illustrationId = R.drawable.img_customer,
                    illustrationAspect = CustomerArtAspect,
                    onClick = { onRoleSelected(UserRole.CUSTOMER) }
                )
                RoleCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = stringResource(R.string.role_worker_title),
                    description = stringResource(R.string.role_worker_description),
                    bento = Bento.Tangerine,
                    illustrationId = R.drawable.img_worker,
                    illustrationAspect = WorkerArtAspect,
                    onClick = { onRoleSelected(UserRole.HELPER) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Makes the session visible: without it there was no way to tell whether the app
            // considered you signed in.
            if (signedInName != null) {
                Text(
                    text = stringResource(R.string.signed_in_as, signedInName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl)
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
            }

            // Language
            Column(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                SectionDividerTitle(text = stringResource(R.string.language_heading))
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    LanguageOption(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.language_english),
                        isSelected = language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) }
                    )
                    LanguageOption(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.language_hindi),
                        isSelected = language == AppLanguage.HINDI,
                        onClick = { onLanguageChange(AppLanguage.HINDI) }
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
                Text(
                    text = stringResource(R.string.language_change_later),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * One role, as a full-colour card: yellow for asking, orange for helping, so the two choices are
 * told apart at a glance. The whole card is the button; the black arrow only marks it as one.
 */
@Composable
private fun RoleCard(
    title: String,
    description: String,
    bento: BentoColors,
    illustrationId: Int,
    illustrationAspect: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FixTheme.colors
    BentoCard(colors = bento, modifier = modifier, onClick = onClick, contentPadding = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Head above the circle, body cut by its curve.
            Box(
                modifier = Modifier
                    .size(width = 112.dp, height = 128.dp)
                    .clip(PopOutCircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(bento.onFill.copy(alpha = 0.08f))
                )
                Image(
                    painter = painterResource(id = illustrationId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(illustrationAspect)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = bento.onFill,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = bento.onFillSecondary,
                textAlign = TextAlign.Center
            )
            // Pushes the action to the bottom so both cards' buttons line up.
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(Spacing.lg))
            Box(
                modifier = Modifier
                    .size(MinTouchTarget)
                    .clip(CircleShape)
                    .background(colors.ink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.role_get_started),
                    tint = colors.onInk,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionDividerTitle(text: String) {
    val colors = FixTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
        Box(modifier = Modifier.padding(start = Spacing.md).size(4.dp).clip(CircleShape).background(colors.primary))
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Spacing.md),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Box(modifier = Modifier.padding(end = Spacing.md).size(4.dp).clip(CircleShape).background(colors.primary))
        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
    }
}

/** One language choice; `selectable` so a screen reader announces which one is active. */
@Composable
private fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FixTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    Surface(
        modifier = modifier
            .height(MinTouchTarget)
            .clip(shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        color = colors.surface,
        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) colors.primary else colors.textPrimary
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(Spacing.sm))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.cd_selected),
                        tint = colors.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/** Faint skyline and two clouds behind the header, fading into the page background. */
@Composable
private fun SkylineBackground(modifier: Modifier = Modifier) {
    val colors = FixTheme.colors
    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(listOf(colors.primarySurface, colors.background)))

        val unit = 1.dp.toPx()
        val baseline = size.height * 0.92f
        val building = colors.primary.copy(alpha = 0.05f)
        // (x position as a fraction of the width, width in dp, height in dp)
        val towers = listOf(
            Triple(0.02f, 34f, 110f), Triple(0.10f, 26f, 160f), Triple(0.16f, 38f, 90f),
            Triple(0.25f, 30f, 135f), Triple(0.70f, 32f, 120f), Triple(0.77f, 24f, 175f),
            Triple(0.83f, 36f, 100f), Triple(0.91f, 28f, 145f)
        )
        towers.forEach { (x, w, h) ->
            drawRect(
                color = building,
                topLeft = Offset(size.width * x, baseline - h * unit),
                size = Size(w * unit, h * unit)
            )
        }
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                1f to colors.background,
                startY = baseline - 80 * unit,
                endY = size.height
            )
        )

        // Kept clear of the logo, wordmark and heading, which sit in the middle third.
        drawCloud(Offset(size.width * 0.80f, 56 * unit), unit * 0.9f, colors.primary.copy(alpha = 0.10f))
        drawCloud(Offset(size.width * 0.04f, 120 * unit), unit * 0.7f, colors.primary.copy(alpha = 0.10f))
    }
}

private fun DrawScope.drawCloud(origin: Offset, unit: Float, color: Color) {
    drawCircle(color, 14 * unit, origin + Offset(14 * unit, 0f))
    drawCircle(color, 20 * unit, origin + Offset(34 * unit, -8 * unit))
    drawCircle(color, 14 * unit, origin + Offset(54 * unit, 0f))
    drawRect(color, topLeft = origin + Offset(14 * unit, -2 * unit), size = Size(40 * unit, 16 * unit))
}
