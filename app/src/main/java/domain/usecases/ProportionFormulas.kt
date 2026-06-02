package com.example.samplesizecalculator.domain.usecases

import com.example.samplesizecalculator.domain.models.SampleSizeResult
import kotlin.math.*

object ProportionFormulas {

    // 8. Single proportion
    // n ≥ Z_{1-α/2}² p(1-p) / d²
    fun singleProportion(alpha: Double, p: Double, d: Double): SampleSizeResult {
        val z = CommonMath.zAlphaTwoSided(alpha)
        val n = z.pow(2.0) * p * (1.0 - p) / d.pow(2.0)
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(total = nInt)
    }

    // 9. Single proportion, finite population
    // n (infinite) as above, nTotal ≥ Nn / (N + n - 1)
    fun singleProportionFinite(
        alpha: Double,
        p: Double,
        d: Double,
        N: Double
    ): SampleSizeResult {
        val base = singleProportion(alpha, p, d).total!!.toDouble()
        val nTotal = (N * base) / (N + base - 1.0)
        val nTotalInt = CommonMath.ceilInt(nTotal)
        return SampleSizeResult(group1 = base.toInt(), total = nTotalInt)
    }

    // 10. Two proportions, equal size
    // p̄ = (p1 + p2)/2
    // n ≥ [ Z_{1-α/2}√(2 p̄(1-p̄)) + Z_{1-β}√(p1(1-p1)+p2(1-p2)) ]² / (p2 - p1)²
    fun compareTwoProportionsEqual(
        alpha: Double,
        beta: Double,
        p1: Double,
        p2: Double
    ): SampleSizeResult {
        val pBar = (p1 + p2) / 2.0
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)

        val term1 = zAlpha * sqrt(2.0 * pBar * (1.0 - pBar))
        val term2 = zBeta * sqrt(p1 * (1.0 - p1) + p2 * (1.0 - p2))
        val n = (term1 + term2).pow(2.0) / (p2 - p1).pow(2.0)

        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(group1 = nInt, group2 = nInt, total = 2 * nInt)
    }

    // 11. >2 proportions, equal size (k groups, effect size ω)
    // p̄ = (p1 + ... + pk)/k
    // ω = sqrt( (1/(k p̄(1-p̄))) Σ (p_i - p̄)² )
    // N_total ≥ (Z_{1-α} + Z_{1-β})² / (k ω²)
    fun compareMoreThanTwoProportionsEqual(
        alpha: Double,
        beta: Double,
        pBar: Double,
        omega: Double,
        K: Int
    ): SampleSizeResult {
        require(K >= 2) { "Number of groups (k) must be at least 2." }
        require(omega > 0.0) { "Effect size (ω) must be positive." }

        val zAlpha = CommonMath.zAlphaOneSided(alpha) // Z_{1-α}
        val zBeta = CommonMath.zBeta(beta)
        val nTotal = (zAlpha + zBeta).pow(2.0) / (K.toDouble() * omega.pow(2.0))
        val nTotalInt = CommonMath.ceilInt(nTotal)
        return SampleSizeResult(total = nTotalInt)
    }

    // 12. Two proportions, unequal group size (r = n2/n1)
    // p = (p1 + r p2)/(1+r)
    // n1 ≥ [ Z_{1-α/2}√((1+r)p(1-p)) + Z_{1-β}√(r p1(1-p1) + p2(1-p2)) ]² /
    //        [ r (p2 - p1)² ]
    fun compareTwoProportionsUnequal(
        alpha: Double,
        beta: Double,
        p1: Double,
        p2: Double,
        r: Double
    ): SampleSizeResult {
        val p = (p1 + r * p2) / (1.0 + r)
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)

        val term1 = zAlpha * sqrt((1.0 + r) * p * (1.0 - p))
        val term2 = zBeta * sqrt(r * p1 * (1.0 - p1) + p2 * (1.0 - p2))
        val n1 = (term1 + term2).pow(2.0) / (r * (p2 - p1).pow(2.0))

        val n1Int = CommonMath.ceilInt(n1)
        val n2Int = CommonMath.ceilInt(r * n1Int)
        return SampleSizeResult(group1 = n1Int, group2 = n2Int, total = n1Int + n2Int)
    }

    // 13. Paired proportions
    // n ≥ (Z_{1-α} + Z_{1-β})² (p01 + p10) / (p01 - p10)²
    fun comparePairedProportions(
        alpha: Double,
        beta: Double,
        p01: Double,
        p10: Double
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaOneSided(alpha) // Z_{1-α}
        val zBeta = CommonMath.zBeta(beta)
        val n = (zAlpha + zBeta).pow(2.0) * (p01 + p10) / (p01 - p10).pow(2.0)
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(total = nInt)
    }
}
