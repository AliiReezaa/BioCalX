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
import com.example.samplesizecalculator.ui.components.*
import com.example.samplesizecalculator.ui.utils.LinkUtils
import kotlin.math.ceil

@Composable
fun EstimateSpecificityScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var spec by remember { mutableStateOf("") }
    var prev by remember { mutableStateOf("") }
    var dText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val s = spec.trim().toDoubleOrNull()
    val p = prev.trim().toDoubleOrNull()
    val d = dText.trim().toDoubleOrNull()

    // ---- validation ----
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val specValid  = (s != null && s > 0.0 && s < 1.0)
    val prevValid  = (p != null && p > 0.0 && p < 1.0)
    val dValid     = (d != null && d > 0.0 && d < 1.0)

    val allValid = alphaValid && specValid && prevValid && dValid

    // ---- calculation ----
    // IMPORTANT: Since we display inequalities (n ≥, N ≥), rounding must be consistent:
    // 1) compute raw n_non_disease
    // 2) ceil it -> integer n_non_disease
    // 3) compute total based on that integer
    // 4) ceil total
    val result = remember(alpha, spec, prev, dText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                // Two-sided Z_{1-α/2}
                val z = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0)

                // Raw non-diseased sample size
                val nNonDiseaseRaw =
                    (z * z * s!! * (1.0 - s)) / (d!! * d)

                // Ceil to satisfy n ≥
                val nNonDisease = ceil(nNonDiseaseRaw).toInt()

                // Total sample size must be derived from the CEILED non-diseased count
                val nTotalRaw =
                    nNonDisease.toDouble() / (1.0 - p!!)

                val nTotal = ceil(nTotalRaw).toInt()

                Pair(nNonDisease, nTotal)

            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // ---- copy/share text ----
    val storeLink = LinkUtils.getStoreLink()

    val copyText = buildString {
        appendLine("BioCalX – Estimate Specificity")
        appendLine("α = ${alpha.trim()}")
        appendLine("Specificity = ${spec.trim()}")
        appendLine("Prevalence = ${prev.trim()}")
        appendLine("Precision (d) = ${dText.trim()}")
        appendLine()
        appendLine("Non-diseased required (n ≥): ${result?.first ?: "—"}")
        appendLine("Total sample size (N ≥): ${result?.second ?: "—"}")
        appendLine()
        append("Calculated using BioCalX: $storeLink")
    }

    CalculatorScaffold(
        title = "Estimate Specificity",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {

            FormulaImage(
                resId = R.drawable.approach16_1,
                contentDescription = "Sample size for estimating specificity"
            )

            FormulaImage(
                resId = R.drawable.approach16_2,
                contentDescription = "Total sample size using prevalence"
            )

            NumericInputField(
                label = "Type I error (α)",
                value = alpha,
                onValueChange = { alpha = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    helperText = "Allowed range: 0 < α < 1 (common: 0.01, 0.05)"
                )
            )

            NumericInputField(
                label = "Expected specificity (Spec)",
                value = spec,
                onValueChange = { spec = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    helperText = "Allowed range: 0 < Spec < 1"
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
                    helperText = "Allowed range: 0 < Prev < 1"
                )
            )

            NumericInputField(
                label = "Precision (d)",
                value = dText,
                onValueChange = { dText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    helperText = "Allowed range: 0 < d < 1 (common: 0.02–0.10)"
                )
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Non-diseased subjects required (n ≥): ${result?.first ?: "—"}")
            Text("Total sample size (N ≥): ${result?.second ?: "—"}")

            Spacer(Modifier.height(16.dp))

            ResultActionsBar(
                enabled = (result != null),
                copyText = copyText,
                shareText = copyText,
                subject = "BioCalX – Estimate Specificity"
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
