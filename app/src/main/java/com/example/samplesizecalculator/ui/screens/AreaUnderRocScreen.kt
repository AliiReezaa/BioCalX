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
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.sqrt

@Composable
fun AreaUnderRocScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var auc by remember { mutableStateOf("") }     // A1 (expected AUC)
    var auc0 by remember { mutableStateOf("") }    // A0 (null AUC)
    var rText by remember { mutableStateOf("") } // R = n_control / n_disease
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val A1 = auc.trim().toDoubleOrNull()
    val A0 = auc0.trim().toDoubleOrNull()
    val Rratio = rText.trim().toDoubleOrNull()

    // ---- validation ----
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val aucValid   = (A1 != null && A1 > 0.0 && A1 < 1.0)
    val auc0Valid  = (A0 != null && A0 > 0.0 && A0 < 1.0)

    val effectValid =
        if (aucValid && auc0Valid) abs(A1!! - A0!!) > 0.0 else false

    val rValid = (Rratio != null && Rratio > 0.0 && Rratio <= 100.0)

    val allValid = alphaValid && betaValid && aucValid && auc0Valid && effectValid && rValid

    val effectHintError =
        if (aucValid && auc0Valid && !effectValid)
            "Expected AUC (A) must be different from null AUC (A₀)."
        else null

    val aucChanceHint =
        if (aucValid && A1!! <= 0.5)
            "Note: AUC ≤ 0.5 is not better than chance; sample size may be huge or meaningless."
        else null

    val rHintError =
        if (Rratio != null && !rValid)
            "Ratio R must be > 0 (R = n_control / n_disease; common: 1, 2, 3)."
        else null

    // ---- compute using NEW formula ----
    data class AucResult(val group1: Int, val group2: Int, val total: Int)

    val result = remember(alpha, beta, auc, auc0, rText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                val zAlpha = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0) // z_{1-α/2}
                val zBeta  = CommonMath.inverseStandardNormal(1.0 - b!!)       // z_{1-β}
                val Rv = Rratio!!

                fun vOf(A: Double): Double {
                    // a = sqrt(2) * Phi^{-1}(A)
                    val aa = sqrt(2.0) * CommonMath.inverseStandardNormal(A)
                    val aa2 = aa * aa
                    // V(A) = 0.0099 * exp(-a^2/2) * [ (5a^2 + 8) + (a^2 + 8)/R ]
                    return 0.0099 * exp(-aa2 / 2.0) * ((5.0 * aa2 + 8.0) + ((aa2 + 8.0) / Rv))
                }

                val V0 = vOf(A0!!)
                val V1 = vOf(A1!!)

                val delta = (A0 - A1)
                val nDiseaseRaw =
                    ((zAlpha * sqrt(V0) + zBeta * sqrt(V1)) * (zAlpha * sqrt(V0) + zBeta * sqrt(V1))) /
                            (delta * delta)

                val nDisease = ceil(nDiseaseRaw).toInt().coerceAtLeast(1)
                val nControl = ceil(Rv * nDiseaseRaw).toInt().coerceAtLeast(1)
                val total = nDisease + nControl

                AucResult(
                    group1 = nDisease,  // disease / positive
                    group2 = nControl,  // control / negative
                    total = total
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

    // ---- text for copy/share ----
    val formulaName = "Area under ROC curve (new V(A) method)"
    val subject = "BioCalX sample size result ($formulaName)"
    val storeLink = LinkUtils.getStoreLink()

    val shareText = buildString {
        appendLine(formulaName)
        appendLine("Inputs:")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("A (expected) = ${auc.trim()}")
        appendLine("A₀ (null) = ${auc0.trim()}")
        appendLine("R (n_control/n_disease) = ${rText.trim()}")
        appendLine()
        appendLine("Result:")
        appendLine("Disease / positive (n_disease ≥): ${result?.group1 ?: "—"}")
        appendLine("Control / negative (n_control ≥): ${result?.group2 ?: "—"}")
        appendLine("Total N: ${result?.total ?: "—"}")
        appendLine()
        append("Calculated using BioCalX: $storeLink")
    }

    val copyText = shareText

    CalculatorScaffold(
        title = "Area under ROC curve",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {

            // Keep your images (you said you updated them already)
            FormulaImage(
                resId = R.drawable.approach17_1,
                contentDescription = "AUC sample size formula"
            )
            FormulaImage(
                resId = R.drawable.approach17_2,
                contentDescription = "V(A) definition"
            )
            FormulaImage(
                resId = R.drawable.approach17_3,
                contentDescription = "Group ratio formula"
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
                label = "Expected AUC (A)",
                value = auc,
                onValueChange = { auc = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < A < 1 (typically A > 0.5)"
                )
            )

            NumericInputField(
                label = "Null AUC (A₀)",
                value = auc0,
                onValueChange = { auc0 = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = false, maxInclusive = false,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 < A₀ < 1 (commonly 0.5)"
                )
            )

            NumericInputField(
                label = "Ratio R = n_control / n_disease",
                value = rText,
                onValueChange = { rText = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 100.0,
                    minInclusive = false, maxInclusive = true,
                    allowNegative = false, allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: R > 0 (common: 1, 2, 3)"
                )
            )

            effectHintError?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            aucChanceHint?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            rHintError?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }
            error?.let { Spacer(Modifier.height(8.dp)); ErrorText(it) }

            Spacer(Modifier.height(16.dp))

            Text("Disease / positive (n_disease ≥): ${result?.group1 ?: "—"}")
            Text("Control / negative (n_control ≥): ${result?.group2 ?: "—"}")
            Text("Total sample size: ${result?.total ?: "—"}")

            ResultActionsBar(
                enabled = (result != null),
                copyText = copyText,
                shareText = shareText,
                subject = subject
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
