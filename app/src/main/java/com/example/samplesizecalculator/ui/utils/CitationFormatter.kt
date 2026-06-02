package com.example.samplesizecalculator.ui.utils

import com.example.samplesizecalculator.domain.models.CitationStyle

object CitationFormatter {

    /**
     * Keep it short and generic until you decide what exact reference you want to cite
     * (e.g., Chow et al., Machin, Lwanga & Lemeshow, Obuchowski ROC, etc.)
     */
    fun footer(style: CitationStyle): String {
        return when (style) {
            CitationStyle.APA ->
                "Citation (APA): BioCalX. (n.d.). Sample size calculator (mobile app)."

            CitationStyle.VANCOUVER ->
                "Citation (Vancouver): BioCalX. Sample size calculator (mobile app) [Internet]. n.d."
        }
    }
}
