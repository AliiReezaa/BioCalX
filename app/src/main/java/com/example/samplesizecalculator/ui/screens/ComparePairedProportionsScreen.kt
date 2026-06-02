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
fun ComparePairedProportionsScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var p01 by remember { mutableStateOf("") }
    var p10 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val a = alpha.trim().toDoubleOrNull()
    val b = beta.trim().toDoubleOrNull()
    val x01 = p01.trim().toDoubleOrNull()
    val x10 = p10.trim().toDoubleOrNull()

    val alphaValid = (a != null && a > 0.0 && a < 1.0)
    val betaValid  = (b != null && b > 0.0 && b < 1.0)

    val p01Valid = (x01 != null && x01 >= 0.0 && x01 <= 1.0)
    val p10Valid = (x10 != null && x10 >= 0.0 && x10 <= 1.0)

    val sumValid = if (p01Valid && p10Valid) (x01!! + x10!!) <= 1.0 else false
    val discordanceValid = if (p01Valid && p10Valid) (x01!! + x10!!) > 0.0 else false
    val diffValid = if (p01Valid && p10Valid) abs(x01!! - x10!!) > 0.0 else false

    val allValid = alphaValid && betaValid && p01Valid && p10Valid && sumValid && discordanceValid && diffValid

    val resultN = remember(alpha, beta, p01, p10) {
        if (!allValid) {
            error = null
            null
        } else {
            try {
                val z1a2 = CommonMath.inverseStandardNormal(1.0 - a!! / 2.0)
                val z1b  = CommonMath.inverseStandardNormal(1.0 - b!!)

                val num = (z1a2 + z1b) * (z1a2 + z1b) * (x01!! + x10!!)
                val den = (x01 - x10) * (x01 - x10)

                val nDouble = num / den
                val n = ceil(nDouble).toInt()

                error = null
                n
            } catch (e: Exception) {
                error = e.message
                null
            }
        }
    }

    val formulaName = "Compare Paired Proportions"
    val storeLink = LinkUtils.getStoreLink()
    val pairsText = resultN?.toString() ?: "—"

    val copyText = buildString {
        appendLine("BioCalX Result")
        appendLine("Formula: $formulaName")
        appendLine()
        appendLine("Inputs:")
        appendLine("α = ${alpha.trim()}")
        appendLine("β = ${beta.trim()}")
        appendLine("p₀₁ = ${p01.trim()}")
        appendLine("p₁₀ = ${p10.trim()}")
        appendLine()
        appendLine("Result:")
        appendLine("Required pairs (n ≥): $pairsText")
        appendLine()
        append("Calculated using BioCalX: $storeLink")
    }

    CalculatorScaffold(
        title = "Compare paired proportions",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {

            FormulaImage(
                resId = R.drawable.approach13,
                contentDescription = "Compare paired proportions sample size formula"
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
                    helperText = "Allowed range: 0 < β < 1 (power = 1−β)"
                )
            )

            NumericInputField(
                label = "Proportion improvement (p₀₁)",
                value = p01,
                onValueChange = { p01 = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = true, maxInclusive = true,
                    allowNegative = false,
                    allowZero = true,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 ≤ p₀₁ ≤ 1"
                )
            )

            NumericInputField(
                label = "Proportion worsening (p₁₀)",
                value = p10,
                onValueChange = { p10 = it },
                constraint = NumericConstraint(
                    min = 0.0, max = 1.0,
                    minInclusive = true, maxInclusive = true,
                    allowNegative = false,
                    allowZero = true,
                    maxDecimals = 6,
                    helperText = "Allowed range: 0 ≤ p₁₀ ≤ 1"
                )
            )

            if (p01Valid && p10Valid && !sumValid) {
                Spacer(Modifier.height(8.dp))
                ErrorText("p₀₁ + p₁₀ must be ≤ 1.")
            }

            if (p01Valid && p10Valid && !discordanceValid) {
                Spacer(Modifier.height(8.dp))
                ErrorText("p₀₁ + p₁₀ must be > 0 (need discordant pairs).")
            }

            if (p01Valid && p10Valid && !diffValid) {
                Spacer(Modifier.height(8.dp))
                ErrorText("p₀₁ and p₁₀ cannot be equal (division by zero).")
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(Modifier.height(16.dp))

            Text(text = "Required pairs (n ≥): $pairsText")

            Spacer(Modifier.height(16.dp))

            ResultActionsBar(
                enabled = (resultN != null),
                copyText = copyText,
                shareText = copyText,
                subject = "BioCalX sample size result ($formulaName)"
            )
        }
    }
}
