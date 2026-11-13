package com.example.ui.screens.account

import android.content.Intent
import androidx.core.net.toUri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.auth.AppUser
import com.example.ui.screens.shell.InitialAvatar
import com.example.ui.theme.*

private val Danger = Color(0xFFC62828)

/**
 * Profile, language, switch role, help, policies, log out and delete account.
 * Help and policy rows appear once their numbers/links are set in gradle.properties.
 */
@Composable
fun AccountScreen(
    user: AppUser?,
    isHelperSide: Boolean,
    isTestMode: Boolean,
    errorText: String?,
    busy: Boolean,
    onSwitchRole: () -> Unit,
    onLogin: () -> Unit,
    onSaveName: (String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val context = LocalContext.current
    val uri = LocalUriHandler.current
    val hindiSoon = stringResource(R.string.auth_hindi_coming_soon)
    var editName by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.account_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
            modifier = Modifier.padding(16.dp))

        // Profile card
        Surface(
            shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGrey),
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            if (user != null) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(user.name ?: "?", size = 56.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name ?: "—", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        Text(user.phone?.let { formatPhone(it) } ?: user.email.orEmpty(), color = SecondaryGrey, fontSize = 14.sp)
                    }
                    TextButton(onClick = { editName = true }) { Text(stringResource(R.string.account_edit), fontWeight = FontWeight.SemiBold) }
                }
            } else {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.account_guest_title), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.account_guest_sub), color = SecondaryGrey, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                        Text(stringResource(R.string.account_login), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (isTestMode) {
            Text(stringResource(R.string.account_test_mode), color = Color(0xFF8A4B00), fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
        errorText?.let { Text(it, color = Danger, fontSize = 14.sp, modifier = Modifier.padding(16.dp)) }

        Section(stringResource(R.string.account_section_app))
        Row_(Icons.Default.Language, stringResource(R.string.account_language), stringResource(R.string.account_language_value)) {
            Toast.makeText(context, hindiSoon, Toast.LENGTH_SHORT).show()
        }
        Row_(
            Icons.Default.SwapHoriz,
            stringResource(if (isHelperSide) R.string.account_switch_to_customer else R.string.account_switch_to_helper),
            stringResource(if (isHelperSide) R.string.account_switch_to_customer_sub else R.string.account_switch_to_helper_sub),
            onClick = onSwitchRole
        )

        val whatsApp = BuildConfig.SUPPORT_WHATSAPP; val phone = BuildConfig.SUPPORT_PHONE; val faq = BuildConfig.FAQ_URL
        if (whatsApp.isNotEmpty() || phone.isNotEmpty() || faq.isNotEmpty()) {
            Section(stringResource(R.string.account_section_help))
            if (whatsApp.isNotEmpty()) Row_(Icons.Default.Chat, stringResource(R.string.account_whatsapp), stringResource(R.string.account_whatsapp_sub)) {
                uri.openUri("https://wa.me/${whatsApp.filter(Char::isDigit)}")
            }
            if (phone.isNotEmpty()) Row_(Icons.Default.Call, stringResource(R.string.account_call), phone) {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
            }
            if (faq.isNotEmpty()) Row_(Icons.AutoMirrored.Filled.HelpOutline, stringResource(R.string.account_faq)) { uri.openUri(faq) }
        }

        val privacy = BuildConfig.PRIVACY_URL; val terms = BuildConfig.TERMS_URL
        if (privacy.isNotEmpty() || terms.isNotEmpty()) Section(stringResource(R.string.account_section_legal))
        if (privacy.isNotEmpty()) Row_(Icons.Default.Lock, stringResource(R.string.account_privacy)) { uri.openUri(privacy) }
        if (terms.isNotEmpty()) Row_(Icons.Default.Description, stringResource(R.string.account_terms)) { uri.openUri(terms) }
        if (user != null) {
            Section(stringResource(R.string.account_section_account))
            Row_(Icons.AutoMirrored.Filled.ExitToApp, stringResource(R.string.account_logout), chevron = false, enabled = !busy, onClick = onLogout)
            Row_(Icons.Default.DeleteOutline, stringResource(R.string.account_delete), stringResource(R.string.account_delete_sub),
                tint = Danger, chevron = false, enabled = !busy) { confirmDelete = true }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp))

        Text(stringResource(R.string.account_version, BuildConfig.VERSION_NAME), color = SecondaryGrey, fontSize = 12.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp))
    }

    if (editName && user != null) {
        var name by rememberSaveable { mutableStateOf(user.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text(stringResource(R.string.account_edit_name_title)) },
            text = {
                OutlinedTextField(name, { name = it.take(40) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
            },
            confirmButton = {
                TextButton(enabled = name.trim().length >= 2, onClick = { onSaveName(name.trim()); editName = false }) {
                    Text(stringResource(R.string.account_save))
                }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text(stringResource(R.string.account_cancel)) } }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.account_delete_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.account_delete_body))
                    if (BuildConfig.ACCOUNT_DELETION_URL.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.account_delete_web, BuildConfig.ACCOUNT_DELETION_URL), fontSize = 13.sp, color = SecondaryGrey)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { confirmDelete = false; onDeleteAccount() }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) {
                    Text(stringResource(R.string.account_delete_confirm))
                }
            },
            dismissButton = { OutlinedButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.account_cancel)) } }
        )
    }
}

private fun formatPhone(e164: String): String {
    val ten = e164.filter(Char::isDigit).takeLast(10)
    return if (ten.length == 10) "+91 ${ten.take(5)} ${ten.drop(5)}" else e164
}

@Composable
private fun Section(title: String) {
    Text(title.uppercase(), color = SecondaryGrey, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))
}

@Composable
private fun Row_(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = DarkNavy,
    chevron: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).background(if (tint == DarkNavy) Color(0xFFEEF4FF) else Color(0xFFFFEBEE), CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = if (tint == DarkNavy) BluePrimary else tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = tint)
            subtitle?.let { Text(it, fontSize = 13.sp, color = SecondaryGrey) }
        }
        if (chevron) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SecondaryGrey)
    }
    HorizontalDivider(color = BorderGrey)
}
