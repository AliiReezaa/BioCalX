package com.example.samplesizecalculator.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SaveResultButton(
    enabled: Boolean,
    label: String = "Save",
    onSave: () -> Unit
) {
    Button(enabled = enabled, onClick = onSave) {
        Text(label)
    }
}
