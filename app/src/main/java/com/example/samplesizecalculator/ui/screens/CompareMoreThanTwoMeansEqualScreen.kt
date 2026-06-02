package com.example.samplesizecalculator.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.R
import com.example.samplesizecalculator.domain.usecases.MeanFormulas
import com.example.samplesizecalculator.ui.components.CalculatorScaffold
import com.example.samplesizecalculator.ui.components.ErrorText
import com.example.samplesizecalculator.ui.components.FormulaImage
import com.example.samplesizecalculator.ui.components.NumericConstraint
import com.example.samplesizecalculator.ui.components.NumericInputField
import com.example.samplesizecalculator.ui.components.ResultActionsBar

@Composable
fun CompareMoreThanTwoMeansEqualScreen(onBack: () -> Unit) {

    var alpha by remember { mutableStateOf("0.05") }
    var beta by remember { mutableStateOf("0.20") }
    var fText by remember { mutableStateOf("") }
    var kText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val result = remember(alpha, beta, fText, kText) {
        val a = alpha.toDoubleOrNull()
        val b = beta.toDoubleOrNull()
        val f = fText.toDoubleOrNull()
        val k = kText.toIntOrNull()

        if (a == null || b == null || f == null || k == null) {
            error = null
            null
        } else {
            try {
                error = null
                MeanFormulas.compareMoreThanTwoMeansEqual(a, b, f, k)
            } catch (e: IllegalArgumentException) {
                error = e.message
                null
            }
        }
    }

    // ---------------------------------------------------------
    // 📤 Copy/Share text (only built when result exists)
    // ---------------------------------------------------------
    val shareText = result?.let {
        """
        📊 Sample Size Calculation – One-way ANOVA (Equal Group Sizes)

        Formula:
        Comparison of more than two means using Cohen’s f (One-way ANOVA)

        Inputs:
        Type I error (α) = ${alpha.trim()}
        Type II error (β) = ${beta.trim()}
        Effect size (f) = ${fText.trim()}
        Number of groups (K) = ${kText.trim()}

        Results:
        Per-group sample size (n ≥) = ${it.group1}
        Total sample size (N) = ${it.total}

        Calculated using BioCalX
        """.trimIndent()
    }

    val actionsEnabled = (result != null && !shareText.isNullOrBlank())

    CalculatorScaffold(
        title = "Compare >2 Means (Equal Size)",
        onBack = onBack
    ) {
        Column {

            // -----------------------------
            // 📐 Formula images
            // -----------------------------
            FormulaImage(
                resId = R.drawable.approach04_1,
                contentDescription = "ANOVA sample size formula – part 1"
            )

            FormulaImage(
                resId = R.drawable.approach04_2,
                contentDescription = "ANOVA sample size formula – part 2"
            )

            // -----------------------------
            // ✏️ Inputs
            // -----------------------------
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
                label = "Effect size (f)",
                value = fText,
                onValueChange = { fText = it },
                constraint = NumericConstraint(
                    min = 0.0,
                    minInclusive = false,
                    allowNegative = false,
                    allowZero = false,
                    maxDecimals = 6,
                    helperText = "Allowed range: f > 0"
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

            // -----------------------------
            // ⚠️ Error
            // -----------------------------
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -----------------------------
            // 📊 Results
            // -----------------------------
            Text(text = "Per-group sample size (n ≥): ${result?.group1 ?: "—"}")
            Text(text = "Total sample size (N): ${result?.total ?: "—"}")

            // -----------------------------
            // 📋 Copy & Share bar
            // -----------------------------
            Spacer(modifier = Modifier.height(12.dp))

            ResultActionsBar(
                modifier = Modifier.fillMaxWidth(),
                enabled = actionsEnabled,
                copyText = shareText ?: "",
                shareText = shareText ?: "",
                subject = "Sample Size Calculation – One-way ANOVA"
            )
        }
    }
}
