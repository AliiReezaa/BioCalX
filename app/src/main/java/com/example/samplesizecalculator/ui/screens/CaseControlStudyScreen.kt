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
fun CaseControlStudyScreen(onBack: () -> Unit) {

    // Inputs
    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var p1 by remember { mutableStateOf("") }       // exposure proportion in controls
    var orText by remember { mutableStateOf("") }   // odds ratio
    var rText by remember { mutableStateOf("") } // controls/cases ratio

    var error by remember { mutableStateOf<String?>(null) }

    // Parsed values (trim = user-friendly, doesn't punish accidental spaces)
    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val pCtrl = p1.trim().toDoubleOrNull()
    val orVal = orText.trim().toDoubleOrNull()
    val r = rText.trim().toDoubleOrNull()

    // Validation (scientific/logical ranges)
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)
    val p1Valid    = (pCtrl != null && pCtrl > 0.0 && pCtrl < 1.0)

    val orPositive = (orVal != null && orVal > 0.0)
    val orNotOne   = (orVal != null && abs(orVal - 1.0) > 0.0)

    // Keep r sane. 0<r<=100 is generous enough for real-world designs.
    val rValid = (r != null && r > 0.0 && r <= 100.0)

    // Optional: implied exposure in cases; if insane, block calculation early
    val pCase =
        if (p1Valid && orPositive) {
            val pc = pCtrl!!
            val OR = orVal!!
            (OR * pc) / (1.0 - pc + OR * pc)
        } else null

    val pCaseValid =
        if (pCase != null) (pCase > 0.0 && pCase < 1.0) else true

    val allValid = alphaValid && betaValid && p1Valid && orPositive && orNotOne && rValid && pCaseValid

    val orOneHint =
        if (orVal != null && !orNotOne) "OR cannot be 1 (no effect to detect)."
        else null

    val rHint =
        if (r != null && !rValid) "Ratio r must be > 0 (controls/cases). Common values: 1, 2, 3."
        else null

    // Compute result ONLY when valid
    val result = remember(alpha, beta, p1, orText, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                StudyDesignFormulas.caseControl(a!!, b!!, pCtrl!!, orVal!!, r!!)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // Copy/share payloads (formula-aware + link)
    val formulaName = "Case-control study (Approach 18)"
    val storeLink = LinkUtils.getStoreLink()

    val copyText = buildString {
        appendLine("BioCalX Sample Size Result")
        appendLine("Formula: $formulaName")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("p₁ (controls) = ${p1.trim()}")
        appendLine("OR = ${orText.trim()}")
        appendLine("r (controls/cases) = ${rText.trim()}")
        appendLine()
        appendLine("Cases (n_case ≥): ${result?.group1 ?: "—"}")
        appendLine("Controls (n_control ≥): ${result?.group2 ?: "—"}")
        appendLine("Total (N): ${result?.total ?: "—"}")
        appendLine()
        append("Calculated using BioCalX: $storeLink")
    }

    // Share can be same as copy, but you may want a tighter subject line
    val subject = "BioCalX Sample Size Result - $formulaName"
    val shareText = copyText

    CalculatorScaffold(
        title = "Case-control study",
        onBack = onBack
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // Formula images
            FormulaImage(
                resId = R.drawable.approach18_1,
                contentDescription = "Case-control sample size formula part 1"
            )
            FormulaImage(
                resId = R.drawable.approach18_2,
                contentDescription = "Case-control sample size formula part 2"
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
                label = "Exposure proportion in controls (p₁)",
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
                label = "Odds ratio (OR)",
                value = orText,
                onValueChange = { orText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 1_000_000.0,
                    minInclusive = false,
                    maxInclusive = true,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: OR > 0 and OR ≠ 1"
                )
            )

            NumericInputField(
                label = "Ratio controls / cases (r)",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 100.0,
                    minInclusive = false,
                    maxInclusive = true,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: r > 0 (r=controls/cases; common: 1, 2, 3)"
                )
            )

            // Inline hints (non-fatal, but helpful)
            orOneHint?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }
            rHint?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            // Exceptions from formula layer
            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            // Results
            Text("Cases (n_case ≥): ${result?.group1 ?: "—"}")
            Text("Controls (n_control ≥): ${result?.group2 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            // Actions (Copy + Share)
            ResultActionsBar(
                enabled = result != null,
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )
        }
    }
}
