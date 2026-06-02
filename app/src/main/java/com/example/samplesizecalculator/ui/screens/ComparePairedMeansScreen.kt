package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlin.math.abs
import kotlin.math.ceil

@Composable
fun ComparePairedMeansScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var delta by remember { mutableStateOf("") }
    var sigmaDiff by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val d = delta.trim().toDoubleOrNull()
    val sd = sigmaDiff.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    // δ can be negative, but cannot be 0 (no effect -> meaningless)
    val deltaValid = (d != null && abs(d) > 0.0)

    // SD of differences must be > 0
    val sdValid = (sd != null && sd > 0.0)

    val allValid = alphaValid && betaValid && deltaValid && sdValid

    val deltaHintError =
        if (d != null && !deltaValid) "δ must be non-zero (effect cannot be 0)."
        else null

    val sdHintError =
        if (sd != null && !sdValid) "σᵈ must be > 0."
        else null

    // ✅ UPDATED: compute directly using the NEW formula
    val nResult = remember(alpha, beta, delta, sigmaDiff) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                error = null

                val zAlpha = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0) // Z_{1-α/2}
                val zBeta  = CommonMath.inverseStandardNormal(1.0 - b!!)       // Z_{1-β}

                val numerator = (zAlpha + zBeta) * (zAlpha + zBeta)            // (Zα + Zβ)^2
                val ratioSq = (sd!! * sd) / (d!! * d)                          // (σd^2 / δ^2)

                ceil(numerator * ratioSq).toInt()
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    CalculatorScaffold(
        title = "Compare paired means (Before–After)",
        onBack = onBack
    ) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            FormulaImage(
                resId = R.drawable.approach07,
                contentDescription = "Compare paired means sample size formula"
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
                label = "Mean of difference (δ)",
                value = delta,
                onValueChange = { delta = it },
                constraint = NumericConstraint(
                    allowNegative = true,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: δ ≠ 0 (can be negative; direction depends on coding)"
                )
            )

            NumericInputField(
                label = "SD of difference (σᵈ)",
                value = sigmaDiff,
                onValueChange = { sigmaDiff = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: σᵈ > 0"
                )
            )

            // Friendly pre-errors
            deltaHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }
            sdHintError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            val formulaName = "Compare paired means (Before–After)"
            val nText = nResult?.toString() ?: "—"

            Text(text = "Required pairs (n ≥): $nText")

            Spacer(Modifier.height(16.dp))

            val storeLink = LinkUtils.getStoreLink()
            val enabled = (nResult != null)

            val copyText = buildString {
                append("BioCalX sample size result\n")
                append("Formula: $formulaName\n")
                append("Inputs: α=$alpha, β=$beta, δ=$delta, σᵈ=$sigmaDiff\n")
                append("Result: Required pairs (n ≥) = $nText\n")
                append("Calculated using BioCalX: $storeLink")
            }

            val shareText = copyText

            ResultActionsBar(
                enabled = enabled,
                copyText = copyText,
                shareText = shareText,
                subject = "BioCalX result: $formulaName"
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
