package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontStyle
import com.example.BuildConfig
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import com.example.data.session.AppLanguage
import com.example.data.session.UserRole
import com.example.ui.theme.*

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@Composable
fun RoleSelectionScreen(
    signedInName: String?,
    onRoleSelected: (UserRole) -> Unit,
    onSignOut: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {

    Scaffold(
        containerColor = FixTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FixTheme.colors.background)
        ) {
            // Decorative Elements: Clouds and Skyline
            DecorativeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Branding Area
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                val appName = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = FixTheme.colors.primary, fontStyle = FontStyle.Italic)) {
                        append(stringResource(R.string.brand_fixora))
                    }
                    withStyle(style = SpanStyle(color = FixTheme.colors.accentGraphic, fontStyle = FontStyle.Italic, fontSize = 56.sp)) {
                        append(stringResource(R.string.brand_x))
                    }
                }
                Text(
                    text = appName,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FixTheme.colors.textPrimary,
                    modifier = Modifier.offset(y = (-28).dp)
                )

                // Devanagari sets taller than Latin at the same point size, so the heading gets a
                // slightly smaller size and tighter leading in Hindi. That is typography, not
                // translation, which is why it stays in code while the words move to resources.
                val isHindi = language == AppLanguage.HINDI
                Text(
                    text = stringResource(R.string.role_heading),
                    fontSize = if (isHindi) 28.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = FixTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isHindi) 36.sp else 38.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.role_subheading),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = FixTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Role Selection Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WideRoleSelectionCard(
                        title = stringResource(R.string.role_customer_title),
                        description = stringResource(R.string.role_customer_description),
                        borderColor = LightBlueBorder,
                        buttonColor = FixTheme.colors.primary,
                        illustrationId = R.drawable.img_customer,
                        onClick = { onRoleSelected(UserRole.CUSTOMER) }
                    )

                    WideRoleSelectionCard(
                        title = stringResource(R.string.role_worker_title),
                        description = stringResource(R.string.role_worker_description),
                        borderColor = LightOrangeBorder,
                        buttonColor = FixTheme.colors.accentGraphic,
                        illustrationId = R.drawable.img_worker,
                        onClick = { onRoleSelected(UserRole.HELPER) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Makes the session visible. Without this there was no way to tell whether the app
                // considered you signed in, and no way to sign out.
                if (signedInName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.signed_in_as, signedInName),
                            fontSize = 14.sp,
                            color = FixTheme.colors.textSecondary
                        )
                        TextButton(onClick = onSignOut) {
                            Text(
                                text = stringResource(R.string.action_sign_out),
                                fontSize = 14.sp,
                                color = FixTheme.colors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Language Selection Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FixTheme.colors.border)
                    Text(
                        text = stringResource(R.string.language_heading),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = FixTheme.colors.textPrimary
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FixTheme.colors.border)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LanguagePill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.language_english),
                        isSelected = language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) }
                    )
                    LanguagePill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.language_hindi),
                        isSelected = language == AppLanguage.HINDI,
                        onClick = { onLanguageChange(AppLanguage.HINDI) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.language_change_later),
                    fontSize = 14.sp,
                    color = FixTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Which build is actually on the device. Without this there is no way to tell
                // whether an update landed, short of reading the system app info screen.
                // Tapping it forces an update check: the automatic one only runs every six hours,
                // so this is how you ask right after a new build is published.
                Text(
                    text =
                        stringResource(
                            R.string.version_footer_tap_to_check,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                    fontSize = 12.sp,
                    color = FixTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    // The bottom spacing sits *before* the clickable so it stays spacing; the
                    // symmetric padding after it is what grows the 12sp line into a 48dp target.
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(Radius.sm))
                        .clickable(role = Role.Button, onClick = onCheckForUpdates)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DecorativeBackground() {
    // Read outside the Canvas: a DrawScope lambda is not composable and cannot reach the theme.
    val skylineColor = FixTheme.colors.primary.copy(alpha = 0.06f)
    val cloudColor = FixTheme.colors.primary.copy(alpha = 0.10f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // City Skyline placeholder
        val buildings = listOf(
            0.1f to 0.3f, 0.2f to 0.5f, 0.3f to 0.4f, 0.4f to 0.6f,
            0.5f to 0.3f, 0.6f to 0.7f, 0.7f to 0.4f, 0.8f to 0.5f, 0.9f to 0.2f
        )
        val baseY = height * 0.55f // Below the heading, behind cards
        for ((xProp, hProp) in buildings) {
            drawRect(
                color = skylineColor,
                topLeft = Offset(width * xProp, baseY - (200f * hProp)),
                size = Size(width * 0.08f, 200f * hProp + height * 0.45f) // Extend down to bottom to be safe
            )
        }

        // Floating clouds placeholder
        fun drawCloud(x: Float, y: Float, scale: Float) {
            drawCircle(cloudColor, 30f * scale, Offset(x, y))
            drawCircle(cloudColor, 40f * scale, Offset(x + 40f * scale, y - 10f * scale))
            drawCircle(cloudColor, 35f * scale, Offset(x + 80f * scale, y))
            drawRect(cloudColor, topLeft = Offset(x, y - 10f * scale), size = Size(80f * scale, 45f * scale))
        }

        drawCloud(width * 0.1f, height * 0.25f, 0.8f) // Left
        drawCloud(width * 0.8f, height * 0.15f, 1.2f) // Top right
    }
}

@Composable
fun CustomLocationLogo(modifier: Modifier = Modifier) {
    val arcColor = FixTheme.colors.accentGraphic
    val pinColor = FixTheme.colors.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.25f // 25% of width
        
        // Custom location pin
        // Orange Upper arc
        drawArc(
            color = arcColor,
            startAngle = 160f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(strokeW/2, strokeW/2),
            size = Size(w - strokeW, h * 0.8f - strokeW),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        
        // Blue lower pin
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.55f)
            lineTo(w * 0.5f, h - strokeW/2)
            lineTo(w * 0.85f, h * 0.4f)
        }
        drawPath(
            path = path,
            color = pinColor,
            style = Stroke(width = strokeW, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WideRoleSelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    borderColor: Color,
    buttonColor: Color,
    illustrationId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.04f),
                ambientColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface),
        border = BorderStroke(1.5.dp, borderColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FixTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = FixTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.role_get_started),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = buttonColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Image(
                painter = painterResource(id = illustrationId),
                contentDescription = title,
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun LanguagePill(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = FixTheme.colors.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) FixTheme.colors.primary else FixTheme.colors.border
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = if (isSelected) FixTheme.colors.primary else FixTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(FixTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.cd_selected),
                        tint = FixTheme.colors.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
