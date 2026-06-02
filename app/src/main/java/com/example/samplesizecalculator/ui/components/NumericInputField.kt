package com.example.samplesizecalculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class NumericConstraint(
    val min: Double? = null,
    val max: Double? = null,
    val minInclusive: Boolean = true,
    val maxInclusive: Boolean = true,
    val allowNegative: Boolean = false,
    val allowZero: Boolean = true,          // for positivity constraints
    val integerOnly: Boolean = false,
    val maxDecimals: Int = 6,
    val helperText: String? = null
)

private fun sanitizeNumericInput(
    raw: String,
    allowNegative: Boolean,
    integerOnly: Boolean,
    maxDecimals: Int
): String {
    // Trim whitespace, but do NOT treat it as an error.
    val t = raw.trim()

    if (t.isEmpty()) return ""

    // Keep only digits + dot + optional leading minus
    val allowed = StringBuilder()
    for ((i, ch) in t.withIndex()) {
        val isMinus = ch == '-'
        val isDot = ch == '.'
        val isDigit = ch in '0'..'9'

        if (isDigit || isDot || isMinus) {
            // Minus only allowed at first char and only if allowNegative
            if (isMinus) {
                if (!allowNegative) continue
                if (i != 0) continue
                if (allowed.isNotEmpty()) continue
            }
            // Dot not allowed if integerOnly
            if (isDot && integerOnly) continue
            allowed.append(ch)
        }
    }

    var s = allowed.toString()

    // Keep only the first dot
    val firstDot = s.indexOf('.')
    if (firstDot != -1) {
        val before = s.substring(0, firstDot + 1)
        val after = s.substring(firstDot + 1).replace(".", "")
        s = before + after
    }

    // Limit decimals
    if (!integerOnly && firstDot != -1) {
        val parts = s.split('.', limit = 2)
        val left = parts[0]
        val right = parts.getOrNull(1) ?: ""
        s = left + "." + right.take(maxDecimals.coerceAtLeast(0))
    }

    return s
}

private fun validateValue(value: String, c: NumericConstraint): String? {
    if (value.isBlank()) return null // empty is NOT an error (screen decides requiredness)

    // Allow user to temporarily type "-" or "." without screaming at them
    if (value == "-" || value == "." || value == "-.") return null

    val v = value.toDoubleOrNull() ?: return "Enter a number"

    if (!c.allowNegative && v < 0) return "Must be ≥ 0"
    if (!c.allowZero && v == 0.0) return "Must be > 0"
    if (c.integerOnly && v % 1.0 != 0.0) return "Must be an integer"

    c.min?.let { min ->
        val ok = if (c.minInclusive) v >= min else v > min
        if (!ok) return if (c.minInclusive) "Must be ≥ $min" else "Must be > $min"
    }

    c.max?.let { max ->
        val ok = if (c.maxInclusive) v <= max else v < max
        if (!ok) return if (c.maxInclusive) "Must be ≤ $max" else "Must be < $max"
    }

    return null
}

@Composable
fun NumericInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    constraint: NumericConstraint = NumericConstraint(),
    modifier: Modifier = Modifier
) {
    val cleaned = sanitizeNumericInput(
        raw = value,
        allowNegative = constraint.allowNegative,
        integerOnly = constraint.integerOnly,
        maxDecimals = constraint.maxDecimals
    )

    // Compute error based on cleaned value (but don't force error on blank)
    val error = validateValue(cleaned, constraint)

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = cleaned,
            onValueChange = {
                val sanitized = sanitizeNumericInput(
                    raw = it,
                    allowNegative = constraint.allowNegative,
                    integerOnly = constraint.integerOnly,
                    maxDecimals = constraint.maxDecimals
                )
                onValueChange(sanitized)
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = (cleaned.isNotBlank() && error != null),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (constraint.integerOnly) KeyboardType.Number else KeyboardType.Decimal
            )
        )

        // Helper text (range) always visible if provided
        constraint.helperText?.let { hint ->
            Text(
                text = hint,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }

        // Error text only if user typed something invalid
        if (cleaned.isNotBlank() && error != null) {
            Text(
                text = error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}
