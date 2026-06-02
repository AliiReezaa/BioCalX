package com.example.samplesizecalculator.ui.utils

import com.example.samplesizecalculator.domain.models.CitationStyle

object ResultTextFormatter {

    data class Input(val label: String, val value: String)

    fun format(
        formulaName: String,
        inputs: List<Input>,
        results: List<Input>,
        includeCitationFooter: Boolean,
        citationStyle: CitationStyle,
        includeBioCalXAttribution: Boolean
    ): String {
        val sb = StringBuilder()

        sb.appendLine("BioCalX Sample Size Calculator")
        sb.appendLine("Formula: $formulaName")
        sb.appendLine()

        sb.appendLine("Inputs:")
        inputs.forEach { sb.appendLine("• ${it.label}: ${it.value.trim()}") }

        sb.appendLine()
        sb.appendLine("Results:")
        results.forEach { sb.appendLine("• ${it.label}: ${it.value.trim()}") }

        if (includeBioCalXAttribution) {
            sb.appendLine()
            sb.appendLine("Calculated using BioCalX")
        }

        if (includeCitationFooter) {
            sb.appendLine()
            sb.appendLine(
                when (citationStyle) {
                    CitationStyle.APA ->
                        "Citation (APA): BioCalX Sample Size Calculator. (Version 1.0.1)."
                    CitationStyle.VANCOUVER ->
                        "Citation (Vancouver): BioCalX Sample Size Calculator; v1.0.1."
                }
            )
        }

        return sb.toString().trim()
    }
}
