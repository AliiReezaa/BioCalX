package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.models.CitationStyle
import com.example.samplesizecalculator.domain.usecases.MeanFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import com.example.samplesizecalculator.ui.utils.ResultTextFormatter

@Composable
fun SingleMeanScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var sigma by remember { mutableStateOf("") }
    var d by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // (Optional) citation toggle state for future wiring
    var includeCitationFooter by rememberSaveable { mutableStateOf(false) }
    var citationStyle by rememberSaveable { mutableStateOf(CitationStyle.APA) }

    val a = alpha.trim().toDoubleOrNull()
    val s = sigma.trim().toDoubleOrNull()
    val dd = d.trim().toDoubleOrNull()

    // Scientific validation
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val sigmaValid = (s != null && s > 0.0)
    val dValid = (dd != null && dd > 0.0)
    val allValid = alphaValid && sigmaValid && dValid

    val result = remember(alpha, sigma, d) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                MeanFormulas.singleMean(a!!, s!!, dd!!)
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // Verbose text (formula name + inputs + result) for copy/share/history/pdf
    val resultText = remember(alpha, sigma, d, result, includeCitationFooter, citationStyle) {
        if (result == null) return@remember ""
        ResultTextFormatter.format(
            formulaName = "Single Mean",
            inputs = listOf(
                ResultTextFormatter.Input("Type I error (α)", alpha.trim()),
                ResultTextFormatter.Input("Standard deviation (σ)", sigma.trim()),
                ResultTextFormatter.Input("Estimation error (d)", d.trim()),
            ),
            results = listOf(
                ResultTextFormatter.Input("Required sample size (n ≥)", result.total.toString())
            ),
            includeCitationFooter = includeCitationFooter,
            citationStyle = citationStyle,
            includeBioCalXAttribution = true
        )
    }

    CalculatorScaffold(
        title = "Single Mean",
        onBack = onBack
    ) {
        Column {

            FormulaImage(
                resId = R.drawable.approach01,
                contentDescription = "Single mean sample size formula"
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
                    helperText = "Allowed range: d > 0"
                )
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text(text = "Required sample size (n ≥): ${result?.total ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // ✅ Your ResultActionsBar expects TEXTS, not callbacks
            ResultActionsBar(
                enabled = result != null,
                copyText = resultText,
                shareText = resultText,
                subject = "BioCalX Sample Size Result"
            )
        }
    }
}
