package com.example.samplesizecalculator.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.ui.utils.ClipboardUtils
import com.example.samplesizecalculator.ui.utils.ShareUtils

@Composable
fun ResultActionsBar(
    enabled: Boolean,
    copyText: String,
    shareText: String,
    subject: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy (primary)
        Button(
            onClick = { copyToClipboard(context, copyText) },
            enabled = enabled && copyText.isNotBlank(),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy result"
            )
            Text(
                text = "Copy result",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Share (secondary)
        OutlinedButton(
            onClick = { ShareUtils.shareText(context, subject, shareText) },
            enabled = enabled && shareText.isNotBlank(),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share result"
            )
            Text(
                text = "Share result",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    // Your ClipboardUtils should handle toast/snackbar feedback.
    ClipboardUtils.copy(context, text)
}
