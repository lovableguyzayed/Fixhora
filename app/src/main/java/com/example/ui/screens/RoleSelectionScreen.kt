package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BrandLogo
import com.example.ui.components.BrandWordmark
import com.example.ui.components.CustomerArtAspect
import com.example.ui.components.PopOutCircleShape
import com.example.ui.components.Spacing
import com.example.ui.components.WorkerArtAspect
import com.example.ui.theme.*

private const val English = "English"
private const val Hindi = "हिंदी"

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    var selectedLanguage by rememberSaveable { mutableStateOf(English) }
    val isHindi = selectedLanguage == Hindi

    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: brand + question, drawn over a faint skyline
            Box(modifier = Modifier.fillMaxWidth()) {
                SkylineBackground(modifier = Modifier.matchParentSize())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screen),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    BrandLogo(height = 64.dp)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    BrandWordmark(fontSize = 30.sp)
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = if (isHindi) "आप कैसे शुरुआत\nकरना चाहेंगे?" else "How would you\nlike to get started?",
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = DarkNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = if (isHindi) "जारी रखने के लिए एक भूमिका चुनें" else "Choose a role to continue",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryGrey,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }

            // Role cards: equal width and equal height, whatever the text length
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                RoleCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = if (isHindi) "मुझे मदद चाहिए" else "I need help",
                    description = if (isHindi) "कार्य पोस्ट करें और पास के भरोसेमंद सहायक पाएं।" else "Post a task and find trusted helpers nearby.",
                    accentColor = BluePrimary,
                    borderColor = LightBlueBorder,
                    illustrationId = R.drawable.img_customer,
                    illustrationAspect = CustomerArtAspect,
                    onClick = { onRoleSelected("user") }
                )
                RoleCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = if (isHindi) "मैं मदद करना चाहता हूँ" else "I want to help",
                    description = if (isHindi) "पास के कार्य खोजें और मदद करके कमाएं।" else "Find tasks nearby and earn by helping others.",
                    accentColor = OrangeSecondary,
                    borderColor = LightOrangeBorder,
                    illustrationId = R.drawable.img_worker,
                    illustrationAspect = WorkerArtAspect,
                    onClick = { onRoleSelected("helper") }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Language selection
            Column(modifier = Modifier.padding(horizontal = Spacing.screen)) {
                SectionDividerTitle(text = if (isHindi) "भाषा चुनें" else "Choose Language")
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    LanguageOption(
                        modifier = Modifier.weight(1f),
                        text = English,
                        isSelected = selectedLanguage == English,
                        onClick = { selectedLanguage = English }
                    )
                    LanguageOption(
                        modifier = Modifier.weight(1f),
                        text = Hindi,
                        isSelected = selectedLanguage == Hindi,
                        onClick = { selectedLanguage = Hindi }
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = if (isHindi) "आप बाद में सेटिंग्स में भाषा बदल सकते हैं" else "You can change the language later in settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = HintGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    accentColor: Color,
    borderColor: Color,
    illustrationId: Int,
    illustrationAspect: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Card(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = DarkNavy.copy(alpha = 0.06f),
            spotColor = DarkNavy.copy(alpha = 0.10f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.sm, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        .background(accentColor.copy(alpha = 0.08f))
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
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = title,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkNavy,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrey,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(Spacing.md))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionDividerTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
        Box(modifier = Modifier.padding(start = Spacing.sm).size(4.dp).clip(CircleShape).background(BluePrimary))
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Spacing.sm),
            style = MaterialTheme.typography.titleMedium,
            color = DarkNavy
        )
        Box(modifier = Modifier.padding(end = Spacing.sm).size(4.dp).clip(CircleShape).background(BluePrimary))
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
    }
}

@Composable
private fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isSelected) BluePrimary else BorderGrey)
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
                color = if (isSelected) BluePrimary else DarkNavy
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(Spacing.xs))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(BluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/** Faint city skyline and clouds behind the header, fading into the white page. */
@Composable
private fun SkylineBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(listOf(MutedBackground, Color.White)))

        val baseline = size.height * 0.92f
        val building = BluePrimary.copy(alpha = 0.045f)
        val unit = 1.dp.toPx()
        // (x position as fraction of width, width dp, height dp)
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
                1f to Color.White,
                startY = baseline - 80 * unit,
                endY = size.height
            )
        )

        drawCloud(Offset(size.width * 0.80f, 56 * unit), unit * 0.9f)
        drawCloud(Offset(size.width * 0.06f, 170 * unit), unit * 0.7f)
    }
}

private fun DrawScope.drawCloud(origin: Offset, unit: Float) {
    val color = LightBlueBorder.copy(alpha = 0.55f)
    drawCircle(color, 14 * unit, origin + Offset(14 * unit, 0f))
    drawCircle(color, 20 * unit, origin + Offset(34 * unit, -8 * unit))
    drawCircle(color, 14 * unit, origin + Offset(54 * unit, 0f))
    drawRect(color, topLeft = origin + Offset(14 * unit, -2 * unit), size = Size(40 * unit, 16 * unit))
}
