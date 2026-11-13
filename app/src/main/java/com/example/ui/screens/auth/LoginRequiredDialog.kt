package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.*

/** Shown when a guest tries to post a task or send an offer. */
@Composable
fun LoginRequiredDialog(
    googleBusy: Boolean,
    errorText: String?,
    onMobile: () -> Unit,
    onGoogle: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.login_required_title)) },
        text = {
            Column {
                Text(stringResource(R.string.login_required_body))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onMobile, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                    Text(stringResource(R.string.auth_continue_mobile), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onGoogle, enabled = !googleBusy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderGrey)) {
                    if (googleBusy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        Image(painterResource(R.drawable.ic_google_g), contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.login_continue_google), color = DarkNavy, fontWeight = FontWeight.SemiBold)
                    }
                }
                errorText?.let { Spacer(Modifier.height(8.dp)); Text(it, color = ErrorRed) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.login_required_later)) } }
    )
}
