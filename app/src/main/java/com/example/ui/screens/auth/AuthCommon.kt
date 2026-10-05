package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.BackButton
import com.example.ui.components.Spacing

/**
 * Shared frame for every auth / onboarding form: respects status, navigation and keyboard
 * insets, shows the same back button, and keeps the content on the 24dp screen gutter.
 */
@Composable
fun AuthScreenLayout(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onBack)
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen),
            horizontalAlignment = horizontalAlignment
        ) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            content()
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
        if (footer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen, vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = footer
            )
        }
    }
}

internal fun String.digitsOnly(maxLength: Int): String = filter { it.isDigit() }.take(maxLength)

internal fun isValidIndianMobile(number: String): Boolean =
    number.length == 10 && number.first() in '6'..'9'

internal fun isValidEmail(email: String): Boolean =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email.trim())
