package com.example.samplesizecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.samplesizecalculator.ui.theme.SampleSizeCalculatorTheme
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

// ---------------------------------------------------------------------
// Simple enum to switch between calculators
// ---------------------------------------------------------------------
enum class CalculatorMode(val label: String) {
    SINGLE_PROPORTION("Single Proportion"),
    SINGLE_MEAN("Single Mean")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleSizeCalculatorTheme {
                Surface(modifier = Modifier.Companion.fillMaxSize()) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {

    // Which calculator is active
    var mode by remember { mutableStateOf(CalculatorMode.SINGLE_PROPORTION) }

    // ------------ Single Proportion state ------------
    var alphaPropText by remember { mutableStateOf("0.05") }
    var pText by remember { mutableStateOf("0.5") }
    var dPropText by remember { mutableStateOf("0.05") }
    var errorProp by remember { mutableStateOf<String?>(null) }

    // ------------ Single Mean state ------------
    var alphaMeanText by remember { mutableStateOf("0.05") }
    var sigmaText by remember { mutableStateOf("1.0") }
    var dMeanText by remember { mutableStateOf("0.5") }
    var errorMean by remember { mutableStateOf<String?>(null) }

    // Recompute both; we only show the active one
    val resultProp = calculateResultProportion(
        alphaStr = alphaPropText,
        pStr = pText,
        dStr = dPropText,
        setError = { errorProp = it }
    )

    val resultMean = calculateResultSingleMean(
        alphaStr = alphaMeanText,
        sigmaStr = sigmaText,
        dStr = dMeanText,
        setError = { errorMean = it }
    )

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Sample Size Calculator",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Companion.Center,
            modifier = Modifier.Companion.fillMaxWidth()
        )

        // Mode selector (two "chips")
        ModeSelector(mode = mode, onModeChange = { mode = it })

        // Formula
        when (mode) {
            CalculatorMode.SINGLE_PROPORTION -> FormulaDisplayProportion()
            CalculatorMode.SINGLE_MEAN -> FormulaDisplaySingleMean()
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        // Card-like surface holding inputs
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (mode) {
                    CalculatorMode.SINGLE_PROPORTION -> {
                        NumericInputField(
                            label = "Alpha (α)",
                            value = alphaPropText,
                            onValueChange = { alphaPropText = it }
                        )

                        NumericInputField(
                            label = "Estimated proportion (p)",
                            value = pText,
                            onValueChange = { pText = it }
                        )

                        NumericInputField(
                            label = "Estimation error (d)",
                            value = dPropText,
                            onValueChange = { dPropText = it }
                        )

                        if (!errorProp.isNullOrEmpty()) {
                            Text(
                                text = errorProp!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.Companion.padding(top = 4.dp)
                            )
                        }
                    }

                    CalculatorMode.SINGLE_MEAN -> {
                        NumericInputField(
                            label = "Type I error (α)",
                            value = alphaMeanText,
                            onValueChange = { alphaMeanText = it }
                        )

                        NumericInputField(
                            label = "Standard deviation (σ)",
                            value = sigmaText,
                            onValueChange = { sigmaText = it }
                        )

                        NumericInputField(
                            label = "Estimation error (d)",
                            value = dMeanText,
                            onValueChange = { dMeanText = it }
                        )

                        if (!errorMean.isNullOrEmpty()) {
                            Text(
                                text = errorMean!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.Companion.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Result section
        Column(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalAlignment = Alignment.Companion.Start
        ) {
            Text(
                text = "Minimum required sample size:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (mode) {
                    CalculatorMode.SINGLE_PROPORTION -> resultProp
                    CalculatorMode.SINGLE_MEAN -> resultMean
                },
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

// ---------------------------------------------------------------------
// Mode selector chips
// ---------------------------------------------------------------------
@Composable
fun ModeSelector(mode: CalculatorMode, onModeChange: (CalculatorMode) -> Unit) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ModeChip(
            text = CalculatorMode.SINGLE_PROPORTION.label,
            selected = mode == CalculatorMode.SINGLE_PROPORTION,
            onClick = { onModeChange(CalculatorMode.SINGLE_PROPORTION) }
        )
        Spacer(modifier = Modifier.Companion.width(12.dp))
        ModeChip(
            text = CalculatorMode.SINGLE_MEAN.label,
            selected = mode == CalculatorMode.SINGLE_MEAN,
            onClick = { onModeChange(CalculatorMode.SINGLE_MEAN) }
        )
    }
}

@Composable
fun ModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.Companion.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.Companion.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Single Proportion:
 *    n ≥ Z²(1−α/2)×p(1−p) / d²    (fraction layout)
 */
@Composable
fun FormulaDisplayProportion() {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // "n ≥"
        Text(
            text = "n ≥ ",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Companion.Italic
        )

        // Fraction: numerator over denominator with a bar
        Column(
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            modifier = Modifier.Companion.widthIn(min = 0.dp, max = 260.dp)
        ) {
            // Numerator: Z²(1 − α/2) × p(1 − p)
            Text(
                text = buildNumeratorAnnotated(),
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Companion.Italic,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )

            // Fraction bar
            Divider(
                modifier = Modifier.Companion
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Denominator: d² (² as superscript)
            Text(
                text = buildAnnotatedString {
                    append("d")
                    pushStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Companion.Superscript,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.8f
                        )
                    )
                    append("2")
                    pop()
                },
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Companion.Italic,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

/**
 * Single Mean:
 *    n ≥ (Z_{1−α/2} × σ / d)²
 * Inline layout with subscript and superscript.
 */
@Composable
fun FormulaDisplaySingleMean() {
    val baseSize = MaterialTheme.typography.bodyLarge.fontSize

    Text(
        text = buildAnnotatedString {
            append("n ≥ (")

            append("Z")
            // subscript 1−α/2
            pushStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Companion.Subscript,
                    fontSize = baseSize * 0.8f
                )
            )
            append("1−α/2")
            pop()

            append(" × σ / d)")

            // superscript 2 outside parentheses
            pushStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Companion.Superscript,
                    fontSize = baseSize * 0.8f
                )
            )
            append("2")
            pop()
        },
        style = MaterialTheme.typography.bodyLarge,
        fontStyle = FontStyle.Companion.Italic,
        textAlign = TextAlign.Companion.Center,
        modifier = Modifier.Companion.fillMaxWidth()
    )
}

/**
 * Builds Z²(1 − α/2) × p(1 − p) with a proper superscript 2 on Z.
 */
@Composable
private fun buildNumeratorAnnotated() = buildAnnotatedString {
    // Z² part
    append("Z")
    pushStyle(
        SpanStyle(
            baselineShift = BaselineShift.Companion.Superscript,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.8f
        )
    )
    append("2")
    pop()

    append("(1 − α/2) × p(1 − p)")
}

@Composable
fun NumericInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Companion.Decimal
        ),
        singleLine = true
    )
}

/**
 * Single Proportion:
 *   n = Z² * p(1-p) / d²
 */
fun calculateResultProportion(
    alphaStr: String,
    pStr: String,
    dStr: String,
    setError: (String?) -> Unit
): String {
    setError(null)

    val alpha = alphaStr.toDoubleOrNull()
    val p = pStr.toDoubleOrNull()
    val d = dStr.toDoubleOrNull()

    if (alpha == null || p == null || d == null) {
        return "—"
    }

    if (alpha <= 0.0 || alpha >= 1.0) {
        setError("Alpha (α) must be between 0 and 1.")
        return "Invalid input"
    }
    if (p <= 0.0 || p >= 1.0) {
        setError("Proportion (p) must be between 0 and 1.")
        return "Invalid input"
    }
    if (d <= 0.0) {
        setError("Error (d) must be a positive number.")
        return "Invalid input"
    }

    val z = inverseStandardNormal(1.0 - alpha / 2.0)
    val nDouble = (z * z) * p * (1.0 - p) / (d * d)

    if (nDouble.isNaN() || nDouble.isInfinite() || nDouble <= 0.0) {
        setError("Could not compute a valid sample size.")
        return "Invalid input"
    }

    val nInt = ceil(nDouble).toInt()
    return nInt.toString()
}

/**
 * Single Mean:
 *   n = (Z_{1−α/2} * σ / d)²
 */
fun calculateResultSingleMean(
    alphaStr: String,
    sigmaStr: String,
    dStr: String,
    setError: (String?) -> Unit
): String {
    setError(null)

    val alpha = alphaStr.toDoubleOrNull()
    val sigma = sigmaStr.toDoubleOrNull()
    val d = dStr.toDoubleOrNull()

    if (alpha == null || sigma == null || d == null) {
        return "—"
    }

    if (alpha <= 0.0 || alpha >= 1.0) {
        setError("Alpha (α) must be between 0 and 1.")
        return "Invalid input"
    }
    if (sigma <= 0.0) {
        setError("Standard deviation (σ) must be positive.")
        return "Invalid input"
    }
    if (d <= 0.0) {
        setError("Error (d) must be a positive number.")
        return "Invalid input"
    }

    val z = inverseStandardNormal(1.0 - alpha / 2.0)
    val term = z * sigma / d
    val nDouble = term.pow(2.0)

    if (nDouble.isNaN() || nDouble.isInfinite() || nDouble <= 0.0) {
        setError("Could not compute a valid sample size.")
        return "Invalid input"
    }

    val nInt = ceil(nDouble).toInt()
    return nInt.toString()
}

/**
 * Inverse CDF of the standard normal distribution using Acklam's approximation.
 */
fun inverseStandardNormal(p: Double): Double {
    require(p > 0.0 && p < 1.0)

    val a1 = -39.69683028665376
    val a2 = 220.9460984245205
    val a3 = -275.9285104469687
    val a4 = 138.357751867269
    val a5 = -30.66479806614716
    val a6 = 2.506628277459239

    val b1 = -54.47609879822406
    val b2 = 161.5858368580409
    val b3 = -155.6989798598866
    val b4 = 66.80131188771972
    val b5 = -13.28068155288572

    val c1 = -7.784894002430293e-03
    val c2 = -0.3223964580411365
    val c3 = -2.400758277161838
    val c4 = -2.549732539343734
    val c5 = 4.374664141464968
    val c6 = 2.938163982698783

    val d1 = 7.784695709041462e-03
    val d2 = 0.3224671290700398
    val d3 = 2.445134137142996
    val d4 = 3.754408661907416

    val pLow = 0.02425
    val pHigh = 1.0 - pLow

    return when {
        p < pLow -> {
            val q = sqrt(-2.0 * ln(p))
            (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                    ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
        }
        p <= pHigh -> {
            val q = p - 0.5
            val r = q * q
            (((((a1 * r + a2) * r + a3) * r + a4) * r + a5) * r + a6) * q /
                    (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1.0)
        }
        else -> {
            val q = sqrt(-2.0 * ln(1.0 - p))
            -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                    ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
        }
    }
}