package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.HintGrey
import com.example.ui.theme.MutedSurface
import com.example.ui.theme.OrangeSecondary
import com.example.ui.theme.SecondaryGrey

/** Spacing tokens on the 8pt grid used by every screen. */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp

    /** Horizontal padding for full-width screen content. */
    val screen = 24.dp
}

val ButtonHeight = 56.dp
val FieldShape = RoundedCornerShape(12.dp)
val CardShape = RoundedCornerShape(16.dp)

// ---------------------------------------------------------------------------
// Brand
// ---------------------------------------------------------------------------

@Composable
fun BrandLogo(modifier: Modifier = Modifier, height: Dp = 72.dp) {
    Image(
        painter = painterResource(id = R.drawable.img_logo),
        contentDescription = "FixoraX logo",
        modifier = modifier.size(width = height * 0.755f, height = height)
    )
}

@Composable
fun BrandWordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 32.sp) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = BluePrimary)) { append("Fixora") }
            withStyle(SpanStyle(color = OrangeSecondary)) { append("X") }
        },
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = fontSize * 1.15f,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        letterSpacing = (-0.5).sp
    )
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = BluePrimary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight),
        shape = FieldShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = Spacing.lg)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            ButtonContent(text, leadingIcon, trailingIcon)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = BluePrimary,
    borderColor: Color = contentColor,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    height: Dp = ButtonHeight,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = FieldShape,
        border = BorderStroke(1.dp, if (enabled) borderColor else BorderGrey),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = HintGrey
        ),
        contentPadding = PaddingValues(horizontal = Spacing.md)
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

/** Outlined button with an arbitrary leading graphic, used for social / alternative sign-in. */
@Composable
fun SocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight),
        shape = FieldShape,
        border = BorderStroke(1.dp, BorderGrey),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
        contentPadding = PaddingValues(horizontal = Spacing.md)
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun RowScope.ButtonContent(text: String, leadingIcon: ImageVector?, trailingIcon: ImageVector?) {
    if (leadingIcon != null) {
        Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(Spacing.xs))
    }
    Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    if (trailingIcon != null) {
        Spacer(modifier = Modifier.width(Spacing.xs))
        Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

/** Circular back button used in every top bar so navigation looks the same everywhere. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MutedSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkNavy,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Text fields
// ---------------------------------------------------------------------------

@Composable
fun fixhoraTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DarkNavy,
    unfocusedTextColor = DarkNavy,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = MutedSurface,
    errorContainerColor = Color.White,
    cursorColor = BluePrimary,
    focusedBorderColor = BluePrimary,
    unfocusedBorderColor = BorderGrey,
    errorBorderColor = ErrorRed,
    focusedLabelColor = BluePrimary,
    unfocusedLabelColor = SecondaryGrey,
    errorLabelColor = ErrorRed,
    focusedLeadingIconColor = BluePrimary,
    unfocusedLeadingIconColor = SecondaryGrey,
    errorLeadingIconColor = ErrorRed,
    focusedTrailingIconColor = SecondaryGrey,
    unfocusedTrailingIconColor = SecondaryGrey,
    focusedPlaceholderColor = HintGrey,
    unfocusedPlaceholderColor = HintGrey,
    focusedSupportingTextColor = SecondaryGrey,
    unfocusedSupportingTextColor = SecondaryGrey,
    errorSupportingTextColor = ErrorRed,
    focusedPrefixColor = DarkNavy,
    unfocusedPrefixColor = DarkNavy,
)

@Composable
fun FixhoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: String? = null,
    errorText: String? = null,
    helperText: String? = null,
    counterText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val supporting = errorText ?: helperText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, maxLines = if (singleLine) 1 else 3) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) } },
        trailingIcon = trailingIcon,
        prefix = prefix?.let { { Text(it) } },
        supportingText = if (supporting != null || counterText != null) {
            {
                Row {
                    Text(supporting.orEmpty(), modifier = Modifier.weight(1f))
                    if (counterText != null) Text(counterText, modifier = Modifier.padding(start = Spacing.xs))
                }
            }
        } else null,
        isError = errorText != null,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = FieldShape,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = fixhoraTextFieldColors()
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    leadingIcon: ImageVector? = null,
    errorText: String? = null,
    helperText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var visible by remember { mutableStateOf(false) }
    FixhoraTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        leadingIcon = leadingIcon,
        errorText = errorText,
        helperText = helperText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (visible) "Hide password" else "Show password",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Headers & sections
// ---------------------------------------------------------------------------

/** Large left-aligned page title used on auth / onboarding screens. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = DarkNavy,
            textAlign = textAlign
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryGrey,
                textAlign = textAlign
            )
        }
    }
}

/** Centered title block used at the top of every task-flow step. */
@Composable
fun StepHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs, bottom = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = DarkNavy,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryGrey,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkNavy,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailing != null) trailing()
    }
}

@Composable
fun OrDivider(modifier: Modifier = Modifier, text: String = "OR") {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = HintGrey,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
    }
}

// ---------------------------------------------------------------------------
// Cards, chips & states
// ---------------------------------------------------------------------------

/** Flat white card with a hairline border: the single card style used for content lists. */
@Composable
fun FixhoraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.White,
    borderColor: Color = BorderGrey,
    shape: RoundedCornerShape = CardShape,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}

/** Tinted, borderless information card (tips, safety notes, etc.). */
@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tint: Color = BluePrimary,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FieldShape,
        color = tint.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.Top) {
            IconBadge(icon = icon, tint = tint, size = 36.dp, iconSize = 20.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                Spacer(modifier = Modifier.height(2.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
            }
        }
    }
}

/** Icon inside a softly tinted rounded square. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(size * 0.3f),
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    Surface(modifier = modifier, color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showDot) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Circle with the person's initials; colour is derived from the name so it stays stable. */
@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val palette = listOf(
        Color(0xFF0B57FF), Color(0xFFFF6B00), Color(0xFF16A34A), Color(0xFF7C3AED),
        Color(0xFF0891B2), Color(0xFFE11D48), Color(0xFF4F46E5), Color(0xFFD97706)
    )
    val color = palette[(name.hashCode() and Int.MAX_VALUE) % palette.size]
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(title, style = MaterialTheme.typography.titleMedium, color = DarkNavy, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = SecondaryGrey, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            Button(
                onClick = onAction,
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = Spacing.lg)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Sticky footer holding the primary action of a screen, separated by a hairline. */
@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = Color.White) {
        Column {
            HorizontalDivider(color = BorderGrey.copy(alpha = 0.7f))
            Column(
                modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.md),
                content = content
            )
        }
    }
}

@Composable
fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = ErrorRed,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------

/** Multi-colour Google "G" mark for the "Continue with Google" button. */
val GoogleIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Google",
        defaultWidth = 20.dp,
        defaultHeight = 20.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        addPath(
            pathData = addPathNodes("M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z"),
            fill = SolidColor(Color(0xFFFFC107))
        )
        addPath(
            pathData = addPathNodes("M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z"),
            fill = SolidColor(Color(0xFFFF3D00))
        )
        addPath(
            pathData = addPathNodes("M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z"),
            fill = SolidColor(Color(0xFF4CAF50))
        )
        addPath(
            pathData = addPathNodes("M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571c0.001-0.001,0.002-0.001,0.003-0.002l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z"),
            fill = SolidColor(Color(0xFF1976D2))
        )
    }.build()
}

/** 40dp button for inline card actions (Accept, Bid, Chat…). */
@Composable
fun CompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    color: Color = BluePrimary,
    leadingIcon: ImageVector? = null,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1)
    }
    val shape = RoundedCornerShape(10.dp)
    val padding = PaddingValues(horizontal = Spacing.md)
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = shape,
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
            contentPadding = padding,
            content = content
        )
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            contentPadding = padding,
            content = content
        )
    }
}

/** Small icon + text pair used for task metadata (distance, time, budget…). */
@Composable
fun MetaItem(icon: ImageVector, text: String, modifier: Modifier = Modifier, color: Color = SecondaryGrey) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = HintGrey, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
