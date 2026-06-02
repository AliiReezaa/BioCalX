package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun SingleMeanFiniteScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var sigma by remember { mutableStateOf("") }
    var d by remember { mutableStateOf("") }
    var population by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Parse (safe; user spaces already trimmed by NumericInputField, but trimming again is harmless)
    val a = alpha.trim().toDoubleOrNull()
    val s = sigma.trim().toDoubleOrNull()
    val dd = d.trim().toDoubleOrNull()
    val nPop = population.trim().toDoubleOrNull()

    // Scientific validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val sigmaValid = (s != null && s > 0.0)
    val dValid = (dd != null && dd > 0.0)

    // Population must be integer and at least 2 for finite correction to be meaningful/stable
    val popValid = (nPop != null && nPop >= 2.0 && nPop % 1.0 == 0.0)

    val allValid = alphaValid && sigmaValid && dValid && popValid

    val result = remember(alpha, sigma, d, population) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                val z = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0)
                val n0 = (z * z * s!! * s) / (dd!! * dd)
                val N = nPop!!.toDouble()

                val nFinite = (N * n0) / (N + n0 - 1.0)
                ceil(nFinite).toInt()

            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val formulaName = "Single Mean (Finite Population)"

    // Build share/copy text only when result exists
    val shareText = remember(result, alpha, sigma, d, population) {
        result?.let { n ->
            buildString {
                append("Formula: $formulaName\n\n")
                append("Inputs:\n")
                append("α (Type I error) = ${alpha.trim()}\n")
                append("σ (Standard deviation) = ${sigma.trim()}\n")
                append("d (Estimation error) = ${d.trim()}\n")
                append("N (Population size) = ${population.trim()}\n\n")
                append("Result:\n")
                append("Required sample size (n ≥) = $n\n\n")
                append("Calculated using BioCalX")
            }
        } ?: ""
    }

    CalculatorScaffold(
        title = formulaName,
        onBack = onBack
    ) {
        Column {

            // Base formula + finite population correction
            FormulaImage(
                resId = R.drawable.approach02_1,
                contentDescription = "Single mean finite population base formula"
            )

            FormulaImage(
                resId = R.drawable.approach02_2,
                contentDescription = "Finite population correction for single mean"
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
                label = "Standard deviation (σ)",
                value = sigma,
                onValueChange = { sigma = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: σ > 0"
                )
            )

            NumericInputField(
                label = "Estimation error (d)",
                value = d,
                onValueChange = { d = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: d > 0 (often d < σ)"
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

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Required sample size (n ≥): ${result ?: "—"}")

            // ✅ Copy + Share row (enabled only when result exists)
            ResultActionsBar(
                enabled = (result != null),
                copyText = shareText,
                shareText = shareText,
                subject = "BioCalX: $formulaName"
            )
        }
    }
}
