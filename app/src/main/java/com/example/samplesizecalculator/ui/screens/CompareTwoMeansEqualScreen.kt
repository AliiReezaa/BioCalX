package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.MeanFormulas
import com.example.samplesizecalculator.ui.components.*
import kotlin.math.abs

@Composable
fun CompareTwoMeansEqualScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var mu1 by remember { mutableStateOf("") }
    var mu2 by remember { mutableStateOf("") }
    var sd1 by remember { mutableStateOf("") }
    var sd2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val m1 = mu1.trim().toDoubleOrNull()
    val m2 = mu2.trim().toDoubleOrNull()
    val s1 = sd1.trim().toDoubleOrNull()
    val s2 = sd2.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid = (b != null && b > 0.0 && b < 1.0)
    val meansValid = (m1 != null && m2 != null)
    val sd1Valid = (s1 != null && s1 > 0.0)
    val sd2Valid = (s2 != null && s2 > 0.0)

    val deltaValid =
        if (m1 != null && m2 != null) abs(m1 - m2) > 0.0 else false

    val allValid =
        alphaValid && betaValid && meansValid && sd1Valid && sd2Valid && deltaValid

    val deltaHintError =
        if (meansValid && !deltaValid) "μ₁ and μ₂ must be different (Δ > 0)." else null

    val result = remember(alpha, beta, mu1, mu2, sd1, sd2) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                MeanFormulas.compareTwoMeansEqual(
                    a!!, b!!, m1!!, m2!!, s1!!, s2!!
                )
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val formulaName = "Compare Two Means (Equal Group Size)"

    val shareText = remember(result, alpha, beta, mu1, mu2, sd1, sd2) {
        result?.let {
            buildString {
                append("Formula: $formulaName\n\n")
                append("Inputs:\n")
                append("α (Type I error) = ${alpha.trim()}\n")
                append("β (Type II error) = ${beta.trim()}\n")
                append("μ₁ = ${mu1.trim()}\n")
                append("μ₂ = ${mu2.trim()}\n")
                append("σ₁ = ${sd1.trim()}\n")
                append("σ₂ = ${sd2.trim()}\n\n")
                append("Results:\n")
                append("Per-group sample size (n ≥) = ${it.group1}\n")
                append("Total sample size = ${it.total}\n\n")
                append("Calculated using BioCalX")
            }
        } ?: ""
    }

    CalculatorScaffold(
        title = formulaName,
        onBack = onBack
    ) {
        Column {

            FormulaImage(
                resId = R.drawable.approach03,
                contentDescription = "Sample size formula for comparing two means (equal size)"
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

            deltaHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Required per group (n ≥): ${result?.group1 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            ResultActionsBar(
                enabled = (result != null),
                copyText = shareText,
                shareText = shareText,
                subject = "BioCalX: $formulaName"
            )
        }
    }
}
