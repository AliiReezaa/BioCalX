package com.example.samplesizecalculator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class CitationStyle {
    APA,
    VANCOUVER
}

@Composable
fun CitationFooterToggle(
    selected: CitationStyle,
    onSelected: (CitationStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {

        RadioButton(
            selected = selected == CitationStyle.APA,
            onClick = { onSelected(CitationStyle.APA) }
        )
        Text("APA")

        Spacer(modifier = Modifier.width(16.dp))

        RadioButton(
            selected = selected == CitationStyle.VANCOUVER,
            onClick = { onSelected(CitationStyle.VANCOUVER) }
        )
        Text("Vancouver")
    }
}
