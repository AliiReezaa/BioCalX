package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.samplesizecalculator.ui.utils.LinkUtils
import kotlin.math.abs

@Composable
fun CompareTwoProportionsUnequalScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var p1 by remember { mutableStateOf("") }
    var p2 by remember { mutableStateOf("") }
    var rText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val x1 = p1.trim().toDoubleOrNull()
    val x2 = p2.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val p1Valid = (x1 != null && x1 > 0.0 && x1 < 1.0)
    val p2Valid = (x2 != null && x2 > 0.0 && x2 < 1.0)

    // Must have a real difference
    val deltaValid = if (p1Valid && p2Valid) abs(x1!! - x2!!) > 0.0 else false

    // Ratio r must be > 0 (sanity cap)
    val rValid = (r != null && r > 0.0 && r <= 100.0)

    val allValid = alphaValid && betaValid && p1Valid && p2Valid && deltaValid && rValid

    val deltaHintError =
        if (p1Valid && p2Valid && !deltaValid)
            "p₁ and p₂ must be different (Δ = |p₁ − p₂| > 0)."
        else null

    val rHintError =
        if (r != null && !rValid)
            "Ratio r must be > 0 (r = n₂ / n₁; common values: 0.5, 1, 2)."
        else null

    val result = remember(alpha, beta, p1, p2, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                ProportionFormulas.compareTwoProportionsUnequal(
                    a!!, b!!, x1!!, x2!!, r!!
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

    val enabled = (result != null)

    // IMPORTANT: your LinkUtils.getStoreLink() is NO-ARG in your project right now.
    val storeLink = remember { LinkUtils.getStoreLink() }

    val copyText = remember(result, alpha, beta, p1, p2, rText) {
        if (!enabled) "" else buildString {
            appendLine("BioCalX Result")
            appendLine("Method: Two proportions (unequal size)")
            appendLine("Inputs: α=$alpha, β=$beta, p₁=$p1, p₂=$p2, r=$rText")
            appendLine("n₁ ≥ ${result?.group1}")
            appendLine("n₂ ≥ ${result?.group2}")
            appendLine("Total N = ${result?.total}")
            append("Calculated using BioCalX: $storeLink")
        }
    }

    val shareText = copyText
    val subject = "BioCalX sample size result (Two proportions, unequal size)"

    CalculatorScaffold(
        title = "Two proportions (unequal size)",
        onBack = onBack
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach12,
                contentDescription = "Two proportions unequal sample size formula"
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
                label = "Type II error (β)",
                value = beta,
                onValueChange = { beta = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < β < 1 (power = 1−β; common β: 0.05, 0.10, 0.20)"
                )
            )

            NumericInputField(
                label = "Proportion in group 1 (p₁)",
                value = p1,
                onValueChange = { p1 = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p₁ < 1"
                )
            )

            NumericInputField(
                label = "Proportion in group 2 (p₂)",
                value = p2,
                onValueChange = { p2 = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p₂ < 1 (must differ from p₁)"
                )
            )

            NumericInputField(
                label = "Ratio n₂ / n₁ (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 100.0,
                    minInclusive = false, maxInclusive = true,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: r > 0 (r = n₂/n₁; common: 1, 2, 3)"
                )
            )

            deltaHintError?.let {
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

            Text("Group 1 size (n₁ ≥): ${result?.group1 ?: "—"}")
            Text("Group 2 size (n₂ ≥): ${result?.group2 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // Copy + Share on the SAME line (handled inside ResultActionsBar)
            ResultActionsBar(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )
        }
    }
}
