package com.example.samplesizecalculator.ui.components

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.samplesizecalculator.ui.utils.ClipboardUtils

@Composable
fun CopyToClipboardButton(
    enabled: Boolean,
    textToCopy: String,
    label: String = "Copy result",
) {
    val context = LocalContext.current

    Button(
        onClick = {
            ClipboardUtils.copy(context, textToCopy)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        },
        enabled = enabled
    ) {
        Text(label)
    }
}
