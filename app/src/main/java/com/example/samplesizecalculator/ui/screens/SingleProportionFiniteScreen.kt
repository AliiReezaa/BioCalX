package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.CommonMath
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import kotlin.math.ceil

@Composable
fun SingleProportionFiniteScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var pText by remember { mutableStateOf("") }
    var dText by remember { mutableStateOf("") }
    var population by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Trim first (user-friendly, avoids "space = error" nonsense)
    val a = alpha.trim().toDoubleOrNull()
    val p = pText.trim().toDoubleOrNull()
    val d = dText.trim().toDoubleOrNull()
    val nPop = population.trim().toDoubleOrNull()

    // Scientific validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val pValid = (p != null && p > 0.0 && p < 1.0)
    val dValid = (d != null && d > 0.0 && d < 1.0)

    // N must be integer ≥ 2
    val nValid = (nPop != null && nPop >= 2.0 && nPop % 1.0 == 0.0)

    val allValid = alphaValid && pValid && dValid && nValid

    val nHintError =
        if (nPop != null && !nValid) "N must be an integer ≥ 2."
        else null

    val result = remember(alpha, pText, dText, population) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                // Single proportion with finite population correction
                val z = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0)
                val n0 = (z * z) * p!! * (1.0 - p) / (d!! * d)
                val N = nPop!!.toDouble()

                val nFinite = (N * n0) / (N + n0 - 1.0)
                val nCeil = ceil(nFinite).toInt()

                // If required n exceeds population, sample everyone (practical cap)
                minOf(nCeil, N.toInt())

            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val context = LocalContext.current
    val enabled = (result != null)

    val formulaName = "Single Proportion (Finite Population Correction)"
    val copyText = if (enabled) {
        buildString {
            append("BioCalX Result\n")
            append("Method: $formulaName\n\n")
            append("Inputs:\n")
            append("α = ${alpha.trim()}\n")
            append("p = ${pText.trim()}\n")
            append("d = ${dText.trim()}\n")
            append("N = ${population.trim()}\n\n")
            append("Output:\n")
            append("Minimum required sample size (n ≥) = $result\n\n")
            append("Calculated using BioCalX")
        }
    } else ""

    val shareText = copyText

    CalculatorScaffold(
        title = "Single Proportion (Finite Population)",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach09_1,
                contentDescription = "Single proportion finite population base formula"
            )

            FormulaImage(
                resId = R.drawable.approach09_2,
                contentDescription = "Single proportion finite population correction"
            )

            NumericInputField(
                label = "Type I error (α)",
                value = alpha,
                onValueChange = { alpha = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < α < 1 (common: 0.01, 0.05)"
                )
            )

            NumericInputField(
                label = "Estimated proportion (p)",
                value = pText,
                onValueChange = { pText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p < 1 (use 0.5 if unknown)"
                )
            )

            NumericInputField(
                label = "Estimation error (d)",
                value = dText,
                onValueChange = { dText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < d < 1 (common: 0.01–0.10)"
                )
            )

            NumericInputField(
                label = "Population size (N)",
                value = population,
                onValueChange = { population = it },
                constraint = NumericConstraint(
                    min = 2.0,
                    minInclusive = true,
                    integerOnly = true,
                    allowNegative = false,
                    allowZero = false,
                    helperText = "Allowed range: N is an integer ≥ 2"
                )
            )

            nHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Minimum required sample size (n ≥): ${result ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // Copy + Share on the same row, enabled only when result exists
            ResultActionsBar(
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = "BioCalX – $formulaName"
            )
        }
    }
}
