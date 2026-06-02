package com.example.samplesizecalculator.domain.usecases

import com.example.samplesizecalculator.domain.models.SampleSizeResult
import kotlin.math.*

object DiagnosticFormulas {

    // 14. Correlation coefficient
    // n ≥ ((Z_{1-α} + Z_{1-β}) / (0.5 ln((1+r)/(1-r))))² + 3
    fun correlation(
        alpha: Double,
        beta: Double,
        r: Double
    ): SampleSizeResult {
        require(r > -1.0 && r < 1.0) { "Correlation (r) must be between -1 and 1." }

        val zAlpha = CommonMath.zAlphaOneSided(alpha) // Z_{1-α}
        val zBeta = CommonMath.zBeta(beta)
        val fisher = 0.5 * ln((1.0 + r) / (1.0 - r))
        val n = ((zAlpha + zBeta) / fisher).pow(2.0) + 3.0
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(total = nInt)
    }

    // 15. Sensitivity
    // n_disease ≥ Z_{1-α/2}² Sens(1-Sens)/d² , n_total = n_disease / Prev
    fun sensitivity(
        alpha: Double,
        sens: Double,
        prev: Double,
        d: Double
    ): SampleSizeResult {
        val z = CommonMath.zAlphaTwoSided(alpha)
        val nDisease = z.pow(2.0) * sens * (1.0 - sens) / d.pow(2.0)
        val nTotal = nDisease / prev
        val nInt = CommonMath.ceilInt(nTotal)
        return SampleSizeResult(total = nInt)
    }

    // 16. Specificity
    // n_non-disease ≥ Z_{1-α/2}² Spec(1-Spec)/d² , n_total = n_non-disease / (1-Prev)
    fun specificity(
        alpha: Double,
        spec: Double,
        prev: Double,
        d: Double
    ): SampleSizeResult {
        val z = CommonMath.zAlphaTwoSided(alpha)
        val nNonDisease = z.pow(2.0) * spec * (1.0 - spec) / d.pow(2.0)
        val nTotal = nNonDisease / (1.0 - prev)
        val nInt = CommonMath.ceilInt(nTotal)
        return SampleSizeResult(total = nInt)
    }

    // Helper for AUC variance V(A) with ratio R = n0/n1
    // a = √2 Φ^{-1}(A)
    // V(A) = 0.0099 exp(-a²/2) [ (5a² + 8) + (a² + 8)/R ]
    private fun aucVariance(A: Double, R: Double): Double {
        require(A > 0.0 && A < 1.0) { "AUC must be between 0 and 1." }
        require(R > 0.0) { "Ratio (r) must be positive." }

        val a = sqrt(2.0) * CommonMath.inverseStandardNormal(A)
        val a2 = a * a
        val expTerm = 0.0099 * exp(-a2 / 2.0)
        return expTerm * ((5.0 * a2 + 8.0) + ((a2 + 8.0) / R))
    }

    // 17. Area under ROC curve
    // n_pos (disease) =
    //   [ Z_{1-α}√V(A0) + Z_{1-β}√V(A1) ]² / (A0 - A1)²
    // n_neg = R * n_pos
    fun areaUnderRoc(
        alpha: Double,
        beta: Double,
        A: Double,   // expected AUC (A1)
        A0: Double,  // null AUC
        r: Double    // R = n0 / n1 (neg / pos)
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaOneSided(alpha) // Z_{1-α}
        val zBeta = CommonMath.zBeta(beta)

        val v0 = aucVariance(A0, r)
        val v1 = aucVariance(A, r)

        val numSqrt = zAlpha * sqrt(v0) + zBeta * sqrt(v1)
        val nPos = numSqrt.pow(2.0) / (A0 - A).pow(2.0)

        val nPosInt = CommonMath.ceilInt(nPos)
        val nNegInt = CommonMath.ceilInt(r * nPosInt)
        return SampleSizeResult(
            group1 = nPosInt,  // positive/disease group
            group2 = nNegInt,  // negative/control group
            total = nPosInt + nNegInt
        )
    }
}
