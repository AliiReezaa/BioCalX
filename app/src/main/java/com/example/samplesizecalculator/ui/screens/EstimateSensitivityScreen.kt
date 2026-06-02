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
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.CommonMath
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import com.example.samplesizecalculator.ui.utils.LinkUtils
import kotlin.math.ceil

@Composable
fun EstimateSensitivityScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var sens by remember { mutableStateOf("") }
    var prev by remember { mutableStateOf("") }
    var dText by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val s = sens.trim().toDoubleOrNull()
    val p = prev.trim().toDoubleOrNull()
    val d = dText.trim().toDoubleOrNull()

    // Validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val sensValid  = (s != null && s > 0.0 && s < 1.0)
    val prevValid  = (p != null && p > 0.0 && p < 1.0)
    val dValid     = (d != null && d > 0.0 && d < 1.0)

    val allValid = alphaValid && sensValid && prevValid && dValid

    data class SensitivityResult(
        val nDisease: Int,
        val total: Int
    )

    val result = remember(alpha, sens, prev, dText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                // ✅ CORRECT (two-sided) formula:
                // n_disease ≥ z_{1-α/2}^2 * Sens(1-Sens) / d^2
                // n_total = n_disease / Prev
                val alphaVal = a!!
                val sensVal = s!!
                val prevVal = p!!
                val dVal = d!!

                // Two-sided critical value
                val z = CommonMath.inverseStandardNormal(1.0 - alphaVal / 2.0) // z_{1-α/2}

                val nDiseaseRaw = (z * z) * sensVal * (1.0 - sensVal) / (dVal * dVal)
                val nDisease = ceil(nDiseaseRaw).toInt()

                val nTotalRaw = nDisease.toDouble() / prevVal
                val nTotal = ceil(nTotalRaw).toInt()

                SensitivityResult(nDisease = nDisease, total = nTotal)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val formulaName = "Estimate Sensitivity (Diagnostic accuracy)"
    val storeLink = LinkUtils.getStoreLink()
    val enabled = result != null

    val copyText = if (enabled) {
        buildString {
            appendLine("BioCalX — $formulaName")
            appendLine("Formula: n_disease ≥ z(1−α/2)^2 * Sens(1−Sens) / d^2,  n_total = n_disease / Prev")
            appendLine()
            appendLine("Inputs:")
            appendLine("α = ${alpha.trim()}   (two-sided)")
            appendLine("Sensitivity = ${sens.trim()}")
            appendLine("Prevalence = ${prev.trim()}")
            appendLine("d = ${dText.trim()}")
            appendLine()
            appendLine("Result:")
            appendLine("Required diseased subjects (n_disease ≥): ${result?.nDisease}")
            appendLine("Total subjects needed (n_total ≥): ${result?.total}")
            appendLine()
            appendLine("Calculated using BioCalX: $storeLink")
        }
    } else ""

    val shareText = copyText
    val subject = "BioCalX result: $formulaName"

    CalculatorScaffold(
        title = "Estimate Sensitivity",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach15_1,
                contentDescription = "Sample size formula for sensitivity – disease group"
            )

            FormulaImage(
                resId = R.drawable.approach15_2,
                contentDescription = "Total sample size formula using prevalence"
            )

            NumericInputField(
                label = "Type I error (α)",
                value = alpha,
                onValueChange = { alpha = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < α < 1 (common: 0.01, 0.05)"
                )
            )

            NumericInputField(
                label = "Sensitivity (Sens)",
                value = sens,
                onValueChange = { sens = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < Sens < 1"
                )
            )

            NumericInputField(
                label = "Disease prevalence (Prev)",
                value = prev,
                onValueChange = { prev = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < Prev < 1"
                )
            )

            NumericInputField(
                label = "Estimation error (d)",
                value = dText,
                onValueChange = { dText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < d < 1 (common: 0.02–0.10)"
                )
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Required diseased subjects (n_disease ≥): ${result?.nDisease ?: "—"}")
            Text("Total subjects needed (n_total ≥): ${result?.total ?: "—"}")

            Spacer(Modifier.height(12.dp))

            ResultActionsBar(
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )
        }
    }
}
