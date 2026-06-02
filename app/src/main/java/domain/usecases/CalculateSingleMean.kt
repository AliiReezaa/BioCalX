package com.example.samplesizecalculator.domain.usecases

import com.example.samplesizecalculator.domain.models.SingleMeanInput
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Use case for calculating sample size for a SINGLE MEAN:
 *
 * n >= ( Z_(1-α/2) * σ / d )^2
 */
class CalculateSingleMean {

    operator fun invoke(input: SingleMeanInput): Double {
        val z = inverseStandardNormal(1.0 - input.alpha / 2.0)
        return (z * input.sigma / input.d).pow(2.0)
    }

    /**
     * Inverse CDF of the standard normal distribution
     * using the Acklam approximation (same as in your proportion code).
     */
    private fun inverseStandardNormal(p: Double): Double {
        require(p > 0.0 && p < 1.0) {
            "p must be in (0, 1). Was $p"
        }

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
}
