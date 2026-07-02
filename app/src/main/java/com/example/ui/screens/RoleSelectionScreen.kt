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
import com.example.ui.theme.*

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    var selectedLanguage by remember { mutableStateOf("English") }

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
                    contentDescription = "FixoraX Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                val appName = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = BluePrimary, fontStyle = FontStyle.Italic)) {
                        append("Fixora")
                    }
                    withStyle(style = SpanStyle(color = OrangeSecondary, fontStyle = FontStyle.Italic, fontSize = 56.sp)) {
                        append("X")
                    }
                }
                Text(
                    text = appName,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy,
                    modifier = Modifier.offset(y = (-28).dp)
                )

                // Heading Section
                val isHindi = selectedLanguage == "हिंदी"
                Text(
                    text = if (isHindi) "आप कैसे शुरुआत\nकरना चाहेंगे?" else "How would you\nlike to get started?",
                    fontSize = if (isHindi) 28.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isHindi) 36.sp else 38.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isHindi) "जारी रखने के लिए एक भूमिका चुनें" else "Choose a role to continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryGrey,
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
                        title = if (isHindi) "मुझे मदद चाहिए" else "I need help",
                        description = if (isHindi) "एक कार्य पोस्ट करें और पास में विश्वसनीय सहायक खोजें।" else "Post a task and find trusted helpers nearby.",
                        borderColor = LightBlueBorder,
                        buttonColor = BluePrimary,
                        illustrationId = R.drawable.img_customer,
                        onClick = { onRoleSelected("user") }
                    )

                    WideRoleSelectionCard(
                        title = if (isHindi) "मैं मदद करना चाहता हूँ" else "I want to help",
                        description = if (isHindi) "पास के कार्य खोजें और दूसरों की मदद करके कमाएं।" else "Find tasks nearby and earn by helping others.",
                        borderColor = LightOrangeBorder,
                        buttonColor = OrangeSecondary,
                        illustrationId = R.drawable.img_worker,
                        onClick = { onRoleSelected("helper") }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Language Selection Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
                    Text(
                        text = if (isHindi) "भाषा चुनें" else "Choose Language",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkNavy
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
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
                        text = "English",
                        isSelected = selectedLanguage == "English",
                        onClick = { selectedLanguage = "English" }
                    )
                    LanguagePill(
                        modifier = Modifier.weight(1f),
                        text = "हिंदी",
                        isSelected = selectedLanguage == "हिंदी",
                        onClick = { selectedLanguage = "हिंदी" }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isHindi) "आप बाद में सेटिंग्स में भाषा बदल सकते हैं" else "You can change the language later in settings",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
            }
        }
    }
}

@Composable
fun DecorativeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // City Skyline placeholder
        val skylineOpacity = 0.06f
        val skylineColor = BluePrimary.copy(alpha = skylineOpacity)
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
        val cloudColor = BluePrimary.copy(alpha = 0.10f)
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
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.25f // 25% of width
        
        // Custom location pin
        // Orange Upper arc
        drawArc(
            color = OrangeSecondary,
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
            color = BluePrimary,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = SecondaryGrey,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Get Started",
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
        color = Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BluePrimary else BorderGrey
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
                color = if (isSelected) BluePrimary else DarkNavy,
                fontWeight = FontWeight.Medium
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
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
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
