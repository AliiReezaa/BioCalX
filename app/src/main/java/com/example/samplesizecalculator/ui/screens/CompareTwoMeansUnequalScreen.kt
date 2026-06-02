package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.MeanFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar

@Composable
fun CompareTwoMeansUnequalScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var mu1 by remember { mutableStateOf("") }
    var mu2 by remember { mutableStateOf("") }
    var sd1 by remember { mutableStateOf("") }
    var sd2 by remember { mutableStateOf("") }
    var rText by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    // ---------------------------
    // Parse + validate (trim-safe)
    // ---------------------------
    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val m1 = mu1.trim().toDoubleOrNull()
    val m2 = mu2.trim().toDoubleOrNull()
    val s1 = sd1.trim().toDoubleOrNull()
    val s2 = sd2.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)
    val mu1Valid   = (m1 != null) // any real value
    val mu2Valid   = (m2 != null) // any real value (difference checked by formula)
    val sd1Valid   = (s1 != null && s1 > 0.0)
    val sd2Valid   = (s2 != null && s2 > 0.0)
    val rValid     = (r != null && r > 0.0)

    val allValid = alphaValid && betaValid && mu1Valid && mu2Valid && sd1Valid && sd2Valid && rValid

    val result = remember(alpha, beta, mu1, mu2, sd1, sd2, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                MeanFormulas.compareTwoMeansUnequal(
                    a!!, b!!, m1!!, m2!!, s1!!, s2!!, r!!
                )
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val shareText = buildString {
        appendLine("BioCalX Sample Size Result")
        appendLine("Approach: Compare two means (unequal size)")
        appendLine("Inputs:")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("μ₁ = ${mu1.trim()}")
        appendLine("μ₂ = ${mu2.trim()}")
        appendLine("σ₁ = ${sd1.trim()}")
        appendLine("σ₂ = ${sd2.trim()}")
        appendLine("r = ${rText.trim()} (n₂/n₁)")
        appendLine()
        appendLine("Results:")
        appendLine("n₁ ≥ ${result?.group1 ?: "—"}")
        appendLine("n₂ ≥ ${result?.group2 ?: "—"}")
        appendLine("Total = ${result?.total ?: "—"}")
        appendLine()
        appendLine("Calculated using BioCalX")
    }

    CalculatorScaffold(
        title = "Compare two means (unequal size)",
        onBack = onBack
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)   // ✅ FIX: makes screen scrollable
                .padding(bottom = 24.dp)       // ✅ FIX: gives breathing room at bottom
        ) {

            FormulaImage(
                resId = R.drawable.approach05,
                contentDescription = "Compare two means unequal sample size formula"
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
                label = "Type II error (β)",
                value = beta,
                onValueChange = { beta = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < β < 1 (power = 1−β; common β: 0.05, 0.10, 0.20)"
                )
            )

            NumericInputField(
                label = "Mean in group 1 (μ₁)",
                value = mu1,
                onValueChange = { mu1 = it },
                constraint = NumericConstraint(
                    allowNegative = true,
                    allowZero = true,
                    maxDecimals = 6,
                    helperText = "Any real value (depends on measurement scale)"
                )
            )

            NumericInputField(
                label = "Mean in group 2 (μ₂)",
                value = mu2,
                onValueChange = { mu2 = it },
                constraint = NumericConstraint(
                    allowNegative = true,
                    allowZero = true,
                    maxDecimals = 6,
                    helperText = "Any real value, but must differ from μ₁ (Δ = |μ₁−μ₂| > 0)"
                )
            )

            NumericInputField(
                label = "SD in group 1 (σ₁)",
                value = sd1,
                onValueChange = { sd1 = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: σ₁ > 0"
                )
            )

            NumericInputField(
                label = "SD in group 2 (σ₂)",
                value = sd2,
                onValueChange = { sd2 = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: σ₂ > 0"
                )
            )

            NumericInputField(
                label = "Ratio n₂ / n₁ (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: r > 0 (r = n₂/n₁; common: 1, 2, 3)"
                )
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Group 1 size (n₁ ≥): ${result?.group1 ?: "—"}")
            Text("Group 2 size (n₂ ≥): ${result?.group2 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // ✅ Buttons remain reachable because the screen scrolls
            ResultActionsBar(
                enabled = (result != null),
                shareText = shareText,
                copyText = shareText,
                subject = "BioCalX sample size result"
            )
        }
    }
}
