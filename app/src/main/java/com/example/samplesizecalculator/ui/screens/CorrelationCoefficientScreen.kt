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
import com.example.samplesizecalculator.domain.usecases.DiagnosticFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import com.example.samplesizecalculator.ui.utils.LinkUtils
import kotlin.math.abs

@Composable
fun CorrelationCoefficientScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var rText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val rRangeValid = (r != null && r > -1.0 && r < 1.0)
    val rNonZero    = (r != null && abs(r) > 0.0)

    val allValid = alphaValid && betaValid && rRangeValid && rNonZero

    val rRangeError =
        if (r != null && !rRangeValid)
            "Correlation must be between −1 and 1 (exclusive)."
        else null

    val rZeroError =
        if (r != null && abs(r) == 0.0)
            "Correlation cannot be zero (no detectable association)."
        else null

    val result = remember(alpha, beta, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                DiagnosticFormulas.correlation(a!!, b!!, r!!)
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
    val formulaName = "Correlation Coefficient"
    val storeLink = LinkUtils.getStoreLink()
    val nText = result?.total?.toString() ?: "—"

    val copyText = buildString {
        appendLine("BioCalX Result")
        appendLine(formulaName)
        appendLine()
        appendLine("Inputs:")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("r = ${rText.trim()}")
        appendLine()
        appendLine("Result:")
        appendLine("Required sample size (n ≥): $nText")
    }

    val shareText = buildString {
        append(copyText)
        appendLine()
        appendLine()
        append("Calculated using BioCalX: ")
        append(storeLink)
    }

    CalculatorScaffold(
        title = "Correlation Coefficient",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach14,
                contentDescription = "Correlation coefficient sample size formula"
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
                label = "Expected correlation (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = -1.0,
                    max = 1.0,
                    minInclusive = false,
                    maxInclusive = false,
                    allowNegative = true,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: −1 < r < 1, r ≠ 0"
                )
            )

            rRangeError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            rZeroError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text(text = "Required sample size (n ≥): ${result?.total ?: "—"}")

            Spacer(Modifier.height(16.dp))

            ResultActionsBar(
                copyText = copyText,
                shareText = shareText,
                subject = "BioCalX sample size result",
                enabled = (result != null)
            )
        }
    }
}
