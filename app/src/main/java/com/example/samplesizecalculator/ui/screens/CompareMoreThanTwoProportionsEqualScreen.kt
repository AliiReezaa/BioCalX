package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun CompareMoreThanTwoProportionsEqualScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var omega by remember { mutableStateOf("") }
    var kText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // ---- parsed values (trimmed) ----
    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val om = omega.trim().toDoubleOrNull()
    val kDouble = kText.trim().toDoubleOrNull()

    // ---- scientific validation ----
    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    // ω: must be > 0 (cap just prevents insane inputs)
    val omegaValid = (om != null && om > 0.0 && om <= 5.0)

    // K: integer >= 3
    val kValid = (kDouble != null && kDouble >= 3.0 && kDouble % 1.0 == 0.0)

    val allValid = alphaValid && betaValid && omegaValid && kValid

    val kHintError =
        if (kDouble != null && !kValid) "K must be an integer ≥ 3." else null

    val omegaHintError =
        if (om != null && !omegaValid)
            "Effect size ω must be > 0 (typical: 0.10 small, 0.30 medium, 0.50 large)."
        else null

    // ---- compute result only when valid ----
    val nTotal = remember(alpha, beta, omega, kText) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                val zAlphaHalf = CommonMath.inverseStandardNormal(1.0 - (a!! / 2.0)) // Z_{1-α/2}
                val zBeta      = CommonMath.inverseStandardNormal(1.0 - b!!)          // Z_{1-β}
                val k = kDouble!!.toInt()

                // n_total >= (Z_{1-α/2} + Z_{1-β})^2 / (k * ω^2)
                val numerator = (zAlphaHalf + zBeta) * (zAlphaHalf + zBeta)
                val denom = k * (om!! * om)

                ceil(numerator / denom).toInt()
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    // ---- text for copy/share ----
    val formulaName = "Compare > 2 proportions (equal size)"
    val storeLink = LinkUtils.getStoreLink()
    val totalText = nTotal?.toString() ?: "—"

    val copyText = buildString {
        appendLine("BioCalX Result")
        appendLine("Formula: $formulaName")
        appendLine("α = ${alpha.trim()}   β = ${beta.trim()}")
        appendLine("ω = ${omega.trim()}   K = ${kText.trim()}")
        appendLine("Total sample size: $totalText")
        appendLine()
        append("Calculated using BioCalX: $storeLink")
    }

    val shareSubject = "BioCalX sample size result ($formulaName)"
    val shareText = copyText

    CalculatorScaffold(
        title = formulaName,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {

            // ---- formulas ----
            FormulaImage(
                resId = R.drawable.approach11_1,
                contentDescription = "Compare >2 proportions equal size main formula"
            )

            FormulaImage(
                resId = R.drawable.approach11_2,
                contentDescription = "Compare >2 proportions equal size effect size definition"
            )

            // ---- inputs ----
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
                label = "Effect size (ω)",
                value = omega,
                onValueChange = { omega = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    max = 5.0,
                    minInclusive = false,
                    maxInclusive = true,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: ω > 0 (≈0.10 small, 0.30 medium, 0.50 large)"
                )
            )

            NumericInputField(
                label = "Number of groups (K)",
                value = kText,
                onValueChange = { kText = it },
                constraint = NumericConstraint(
                    min = 3.0,
                    minInclusive = true,
                    integerOnly = true,
                    allowNegative = false,
                    allowZero = false,
                    helperText = "Allowed range: K is an integer ≥ 3"
                )
            )

            // ---- friendly hints ----
            kHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            omegaHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            // ---- result ----
            Text(text = "Total sample size: ${nTotal ?: "—"}")

            Spacer(Modifier.height(16.dp))

            // ---- actions ----
            ResultActionsBar(
                enabled = (nTotal != null),
                copyText = copyText,
                shareText = shareText,
                subject = shareSubject,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
