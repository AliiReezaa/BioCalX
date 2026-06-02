package com.example.samplesizecalculator.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun FormulaImage(
    @DrawableRes resId: Int,
    contentDescription: String
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .heightIn(max = 180.dp),
        contentScale = ContentScale.Fit
    )
}
