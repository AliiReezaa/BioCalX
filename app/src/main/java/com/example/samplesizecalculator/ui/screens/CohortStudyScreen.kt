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
import com.example.samplesizecalculator.domain.usecases.StudyDesignFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar
import com.example.samplesizecalculator.ui.utils.LinkUtils
import kotlin.math.abs

@Composable
fun CohortStudyScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var p1 by remember { mutableStateOf("") }        // outcome proportion in unexposed
    var rrText by remember { mutableStateOf("") }    // relative risk
    var rText by remember { mutableStateOf("") }  // unexposed / exposed
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val pUnexp = p1.trim().toDoubleOrNull()
    val rr = rrText.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val p1Valid = (pUnexp != null && pUnexp > 0.0 && pUnexp < 1.0)

    val rrPositive = (rr != null && rr > 0.0)
    val rrNotOne   = (rr != null && abs(rr - 1.0) > 0.0)

    // ratio unexposed/exposed: scientifically it can be >1, but UI-wise cap to avoid nonsense typos
    val rValid = (r != null && r > 0.0 && r <= 100.0)

    // Implied risk in exposed group p2 = RR * p1 (must stay in (0,1))
    val pExp = if (p1Valid && rrPositive) pUnexp!! * rr!! else null
    val pExpValid = if (pExp != null) (pExp > 0.0 && pExp < 1.0) else true

    val allValid =
        alphaValid &&
                betaValid &&
                p1Valid &&
                rrPositive &&
                rrNotOne &&
                pExpValid &&
                rValid

    val rrOneHint =
        if (rr != null && !rrNotOne) "RR cannot be 1 (no effect to detect)."
        else null

    val pExpHint =
        if (pExp != null && !pExpValid) "Implied risk in exposed group (RR × p₁) must be between 0 and 1."
        else null

    val rHint =
        if (r != null && !rValid) "Ratio r must be > 0 (unexposed / exposed). Common values: 1, 2, 3."
        else null

    val result = remember(alpha, beta, p1, rrText, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                StudyDesignFormulas.cohort(a!!, b!!, pUnexp!!, rr!!, r!!)
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
    val storeLink = LinkUtils.getStoreLink()
    val formulaName = "Cohort Study (Approach 19)"

    val copyText = if (enabled) {
        buildString {
            appendLine("BioCalX Sample Size Result")
            appendLine(formulaName)
            appendLine()
            appendLine("Inputs:")
            appendLine("α = ${a ?: ""}")
            appendLine("β = ${b ?: ""}")
            appendLine("p₁ (unexposed) = ${pUnexp ?: ""}")
            appendLine("RR = ${rr ?: ""}")
            appendLine("r (unexposed/exposed) = ${r ?: ""}")
            appendLine()
            appendLine("Results:")
            appendLine("Exposed (n₁ ≥): ${result?.group1}")
            appendLine("Unexposed (n₂ ≥): ${result?.group2}")
            appendLine("Total (N): ${result?.total}")
            appendLine()
            append("Calculated using BioCalX: $storeLink")
        }
    } else ""

    // Share text can be identical to copy text (most apps prefer plain text + URL)
    val shareText = copyText
    val subject = "BioCalX result: $formulaName"

    CalculatorScaffold(
        title = "Cohort Study",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            // Formula images
            FormulaImage(
                resId = R.drawable.approach19_1,
                contentDescription = "Cohort study sample size formula part 1"
            )
            FormulaImage(
                resId = R.drawable.approach19_2,
                contentDescription = "Cohort study sample size formula part 2"
            )
            FormulaImage(
                resId = R.drawable.approach19_3,
                contentDescription = "Cohort study sample size formula part 3"
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
                label = "Outcome proportion in unexposed (p₁)",
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
                label = "Relative Risk (RR)",
                value = rrText,
                onValueChange = { rrText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1_000_000.0,
                    minInclusive = false, maxInclusive = true,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: RR > 0 and RR ≠ 1"
                )
            )

            NumericInputField(
                label = "Ratio unexposed / exposed (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 100.0,
                    minInclusive = false, maxInclusive = true,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: r > 0 (r = unexposed/exposed; common: 1, 2, 3)"
                )
            )

            rrOneHint?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            pExpHint?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            rHint?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            error?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }

            Spacer(Modifier.height(16.dp))

            Text(text = "Exposed (n₁ ≥): ${result?.group1 ?: "—"}")
            Text(text = "Unexposed (n₂ ≥): ${result?.group2 ?: "—"}")
            Text(text = "Total sample size: ${result?.total ?: "—"}")

            // ✅ Copy + Share on one line (your ResultActionsBar already does this)
            ResultActionsBar(
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
