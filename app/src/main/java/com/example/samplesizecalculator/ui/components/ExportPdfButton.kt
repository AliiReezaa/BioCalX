package com.example.samplesizecalculator.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExportPdfButton(
    enabled: Boolean,
    label: String = "Export PDF",
    onExport: () -> Unit
) {
    Button(enabled = enabled, onClick = onExport) {
        Text(label)
    }
}
