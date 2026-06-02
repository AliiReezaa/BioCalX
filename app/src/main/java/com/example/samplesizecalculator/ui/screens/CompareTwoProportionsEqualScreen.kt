package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.ProportionFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import com.example.samplesizecalculator.ui.utils.LinkUtils.getStoreLink
import kotlin.math.abs

@Composable
fun CompareTwoProportionsEqualScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var p1 by remember { mutableStateOf("") }
    var p2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val x1 = p1.trim().toDoubleOrNull()
    val x2 = p2.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val p1Valid = (x1 != null && x1 > 0.0 && x1 < 1.0)
    val p2Valid = (x2 != null && x2 > 0.0 && x2 < 1.0)

    // Need a non-zero difference in proportions
    val deltaValid = if (p1Valid && p2Valid) abs(x1!! - x2!!) > 0.0 else false
    val allValid = alphaValid && betaValid && p1Valid && p2Valid && deltaValid

    val deltaHintError =
        if (p1Valid && p2Valid && !deltaValid) "p₁ and p₂ must be different (Δ = |p₁−p₂| > 0)."
        else null

    val result = remember(alpha, beta, p1, p2) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                ProportionFormulas.compareTwoProportionsEqual(a!!, b!!, x1!!, x2!!)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // ---- Copy/Share payloads (only meaningful when result != null) ----
    val formulaName = "Compare two proportions (equal size)"
    val storeLink = getStoreLink()

    val copyText = buildString {
        appendLine("BioCalX Result")
        appendLine("Formula: $formulaName")
        appendLine("Inputs:")
        appendLine("  α = ${alpha.trim()}")
        appendLine("  β = ${beta.trim()}")
        appendLine("  p₁ = ${p1.trim()}")
        appendLine("  p₂ = ${p2.trim()}")
        appendLine("Results:")
        appendLine("  Per-group n ≥ ${result?.group1 ?: "—"}")
        appendLine("  Total N = ${result?.total ?: "—"}")
    }

    val shareText = buildString {
        append(copyText.trimEnd())
        appendLine()
        appendLine()
        appendLine("Calculated using BioCalX: $storeLink")
    }

    CalculatorScaffold(
        title = formulaName,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {

            FormulaImage(
                resId = R.drawable.approach10_1,
                contentDescription = "Compare two proportions equal size base formula"
            )

            FormulaImage(
                resId = R.drawable.approach10_2,
                contentDescription = "Compare two proportions equal size pooled proportion"
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
                label = "Proportion in group 1 (p₁)",
                value = p1,
                onValueChange = { p1 = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p₁ < 1"
                )
            )

            NumericInputField(
                label = "Proportion in group 2 (p₂)",
                value = p2,
                onValueChange = { p2 = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p₂ < 1 (must differ from p₁)"
                )
            )

            deltaHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Per-group size (n ≥): ${result?.group1 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            Spacer(Modifier.height(12.dp))

            // Copy + Share on the same line (enabled only when result exists)
            ResultActionsBar(
                enabled = (result != null),
                copyText = copyText,
                shareText = shareText,
                subject = "BioCalX sample size result ($formulaName)"
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
