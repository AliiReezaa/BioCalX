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
fun SurvivalStudyScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var hrText by remember { mutableStateOf("") }
    var pEventText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val hr = hrText.trim().toDoubleOrNull()
    val pEvent = pEventText.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid = (b != null && b > 0.0 && b < 1.0)

    val hrPositive = (hr != null && hr > 0.0)
    val hrNotOne = (hr != null && abs(hr - 1.0) > 0.0)

    val pEventValid = (pEvent != null && pEvent > 0.0 && pEvent < 1.0)

    val allValid = alphaValid && betaValid && hrPositive && hrNotOne && pEventValid

    val hrHint =
        if (hr != null && !hrNotOne)
            "HR = 1 implies no difference; required events become infinite."
        else null

    val pEventHint =
        if (pEvent != null && !pEventValid)
            "Event proportion must be between 0 and 1 (exclusive)."
        else null

    val result = remember(alpha, beta, hrText, pEventText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null
                StudyDesignFormulas.survival(a!!, b!!, hr!!, pEvent!!)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // Build copy/share payloads only when result exists
    val storeLink = remember { LinkUtils.getStoreLink() }
    val hasResult = result != null

    val copyText = remember(alpha, beta, hrText, pEventText, result) {
        if (!hasResult) "" else """
Survival Study (Log-rank / HR-based)

Inputs:
α = ${alpha.trim()}
β = ${beta.trim()}
HR = ${hrText.trim()}
Event proportion (p) = ${pEventText.trim()}

Results:
Required number of events: ${result!!.eventCount}
Per-group sample size (n1 = n2 ≥): ${result!!.group1}
Total sample size (N): ${result!!.total}
        """.trim()
    }

    val shareText = remember(copyText, storeLink) {
        if (!hasResult) "" else """
$copyText

Calculated using BioCalX: $storeLink
        """.trim()
    }

    CalculatorScaffold(
        title = "Survival Study",
        onBack = onBack
    ) {
        // ✅ THIS is the fix for your “can’t scroll down” problem
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach20_1,
                contentDescription = "Survival study sample size formula part 1"
            )
            FormulaImage(
                resId = R.drawable.approach20_2,
                contentDescription = "Survival study sample size formula part 2"
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
                label = "Hazard Ratio (HR)",
                value = hrText,
                onValueChange = { hrText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1_000_000.0,
                    minInclusive = false, maxInclusive = true,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: HR > 0 and HR ≠ 1"
                )
            )

            NumericInputField(
                label = "Expected event proportion (p)",
                value = pEventText,
                onValueChange = { pEventText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < p < 1"
                )
            )

            hrHint?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }
            pEventHint?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text("Required number of events: ${result?.eventCount ?: "—"}")
            Text("Per-group sample size (n₁ = n₂ ≥): ${result?.group1 ?: "—"}")
            Text("Total sample size (N): ${result?.total ?: "—"}")

            // ✅ Copy + Share on same row (your ResultActionsBar already does that)
            ResultActionsBar(
                enabled = hasResult,
                copyText = copyText,
                shareText = shareText,
                subject = "BioCalX result: Survival Study"
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
