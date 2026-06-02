package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
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
fun CompareTwoMeansEffectSizeScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var dText by remember { mutableStateOf("") }
    var rText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val d = dText.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    // Scientific validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid = (b != null && b > 0.0 && b < 1.0)

    // Cohen's d must be > 0 (sanity cap optional)
    val dValid = (d != null && d > 0.0 && d <= 10.0)

    // Ratio must be > 0 (sanity cap optional)
    val rValid = (r != null && r > 0.0 && r <= 100.0)

    val allValid = alphaValid && betaValid && dValid && rValid

    val dHintError =
        if (d != null && !dValid) "Effect size d must be > 0 (typical: 0.2 small, 0.5 medium, 0.8 large)."
        else null

    val rHintError =
        if (r != null && !rValid) "Ratio r must be > 0 (r = n₂/n₁; common: 1, 2, 3)."
        else null

    val result = remember(alpha, beta, dText, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                // IMPORTANT: positional args to avoid named-parameter mismatches
                MeanFormulas.compareTwoMeansEffectSize(a!!, b!!, d!!, r!!)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val enabled = result != null

    // Text payloads for Copy/Share
    val subject = "BioCalX – Compare two means (effect size)"
    val shareText = buildString {
        appendLine("Compare two means (effect size)")
        appendLine("Inputs:")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("d = ${dText.trim()}")
        appendLine("r = ${rText.trim()}  (n₂/n₁)")
        appendLine()
        appendLine("Results:")
        appendLine("n₁ ≥ ${result?.group1 ?: "—"}")
        appendLine("n₂ ≥ ${result?.group2 ?: "—"}")
        appendLine("Total N = ${result?.total ?: "—"}")
        appendLine()
        append("Calculated using BioCalX")
    }

    val copyText = buildString {
        appendLine("Compare two means (effect size)")
        appendLine("n₁ ≥ ${result?.group1 ?: "—"}")
        appendLine("n₂ ≥ ${result?.group2 ?: "—"}")
        appendLine("Total N = ${result?.total ?: "—"}")
        append("Calculated using BioCalX")
    }

    CalculatorScaffold(
        title = "Compare two means (effect size)",
        onBack = onBack
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {

            // 🔹 Formula Images
            FormulaImage(
                resId = R.drawable.approach06_1,
                contentDescription = "Effect size formula part 1"
            )

            FormulaImage(
                resId = R.drawable.approach06_2,
                contentDescription = "Effect size formula part 2"
            )

            // 🔹 Input Fields
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
                label = "Effect size (d)",
                value = dText,
                onValueChange = { dText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 10.0,          // sanity cap; change/remove if you want
                    minInclusive = false,
                    maxInclusive = true,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: d > 0 (typical: 0.2 small, 0.5 medium, 0.8 large)"
                )
            )

            NumericInputField(
                label = "Ratio n₂ / n₁ (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 100.0,         // sanity cap; change/remove if you want
                    minInclusive = false,
                    maxInclusive = true,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: r > 0 (r = n₂/n₁; common: 1, 2, 3)"
                )
            )

            // Friendly errors (before formula throws)
            dHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            rHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 Results
            Text(text = "Group 1 size (n₁ ≥): ${result?.group1 ?: "—"}")
            Text(text = "Group 2 size (n₂ ≥): ${result?.group2 ?: "—"}")
            Text(text = "Total sample size: ${result?.total ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // 🔹 Copy + Share (same row) – enabled only when result exists
            ResultActionsBar(
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
