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
fun SingleProportionScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var pText by remember { mutableStateOf("") }
    var dText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val p = pText.trim().toDoubleOrNull()
    val d = dText.trim().toDoubleOrNull()

    // Scientific validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val pValid = (p != null && p > 0.0 && p < 1.0)
    val dValid = (d != null && d > 0.0 && d < 1.0)

    val allValid = alphaValid && pValid && dValid

    val result = remember(alpha, pText, dText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                val z = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0)
                val nDouble = (z * z) * p!! * (1.0 - p) / (d!! * d)
                ceil(nDouble).toInt()
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // ---- Copy/Share strings (verbose, with formula name) ----
    val formulaName = "Single Proportion (Wald approximation): n = Z_(1-α/2)^2 × p(1-p) / d^2"
    val storeLink = LinkUtils.getStoreLink()

    val copyText = buildString {
        appendLine("BioCalX – Result")
        appendLine("Approach: $formulaName")
        appendLine("Inputs:")
        appendLine("  α = ${alpha.trim()}")
        appendLine("  p = ${pText.trim()}")
        appendLine("  d = ${dText.trim()}")
        appendLine("Output:")
        appendLine("  n ≥ ${result ?: "—"}")
        appendLine()
        appendLine("Calculated using BioCalX: $storeLink")
    }

    val shareText = copyText
    val subject = "BioCalX sample size result – Single Proportion"

    CalculatorScaffold(
        title = "Single Proportion",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {

            FormulaImage(
                resId = R.drawable.approach08,
                contentDescription = "Single proportion sample size formula"
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

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Minimum required sample size (n ≥): ${result ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // Copy + Share (same line, enabled only when result exists)
            ResultActionsBar(
                copyText = copyText,
                shareText = shareText,
                subject = subject,
                enabled = (result != null)
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
