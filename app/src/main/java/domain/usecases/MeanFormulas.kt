package com.example.samplesizecalculator.domain.usecases

import com.example.samplesizecalculator.domain.models.SampleSizeResult
import kotlin.math.*

object MeanFormulas {

    // 1. Single mean
    // n ≥ (Z_{1-α/2} * σ / d)^2
    fun singleMean(alpha: Double, sigma: Double, d: Double): SampleSizeResult {
        val z = CommonMath.zAlphaTwoSided(alpha)
        val n = (z * sigma / d).pow(2.0)
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(group1 = nInt, total = nInt)
    }

    // 2. Single mean in finite population
    // n = (Z_{1-α/2} * σ / d)^2 , nTotal ≥ Nn / (N + n - 1)
    fun singleMeanFinite(
        alpha: Double,
        sigma: Double,
        d: Double,
        N: Double
    ): SampleSizeResult {
        val base = singleMean(alpha, sigma, d).total!!.toDouble()
        val nTotal = (N * base) / (N + base - 1.0)
        val nTotalInt = CommonMath.ceilInt(nTotal)
        return SampleSizeResult(group1 = base.toInt(), total = nTotalInt)
    }

    // 3. Two means, equal group sizes
    // n ≥ (Z_{1-α/2} + Z_{1-β})^2 (σ1² + σ2²) / (μ1 - μ2)²
    fun compareTwoMeansEqual(
        alpha: Double,
        beta: Double,
        mu1: Double,
        mu2: Double,
        sigma1: Double,
        sigma2: Double
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val numerator = (zAlpha + zBeta).pow(2.0) * (sigma1.pow(2.0) + sigma2.pow(2.0))
        val denom = (mu1 - mu2).pow(2.0)
        val n = numerator / denom
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(group1 = nInt, group2 = nInt, total = 2 * nInt)
    }

    // 4. >2 means, equal group sizes (ANOVA, effect size f)
    // N ≈ (Z_{1-α} + Z_{1-β})² (K - 1) / (K f²),  n = N / K
    fun compareMoreThanTwoMeansEqual(
        alpha: Double,
        beta: Double,
        f: Double,
        K: Int
    ): SampleSizeResult {
        require(K >= 2) { "Number of groups (K) must be at least 2." }
        require(f > 0.0) { "Effect size (f) must be positive." }

        val zAlpha = CommonMath.zAlphaOneSided(alpha) // Z_{1-α}
        val zBeta = CommonMath.zBeta(beta)            // Z_{1-β}

        val numerator = (zAlpha + zBeta).pow(2.0) * (K - 1.0)
        val denominator = K.toDouble() * f.pow(2.0)

        val nTotal = numerator / denominator
        val nPerGroup = nTotal / K.toDouble()

        val nPerGroupInt = CommonMath.ceilInt(nPerGroup)
        val nTotalInt = CommonMath.ceilInt(nTotal)

        return SampleSizeResult(group1 = nPerGroupInt, total = nTotalInt)
    }

    // 5. Two means, unequal group sizes (r = n2/n1)
    // n1 ≥ (Z_{1-α/2} + Z_{1-β})² (σ1² + σ2² / r) / (μ1 - μ2)²
    fun compareTwoMeansUnequal(
        alpha: Double,
        beta: Double,
        mu1: Double,
        mu2: Double,
        sigma1: Double,
        sigma2: Double,
        r: Double
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val numerator = (zAlpha + zBeta).pow(2.0) *
                (sigma1.pow(2.0) + sigma2.pow(2.0) / r)
        val denom = (mu1 - mu2).pow(2.0)
        val n1 = numerator / denom
        val n1Int = CommonMath.ceilInt(n1)
        val n2Int = CommonMath.ceilInt(r * n1Int)
        return SampleSizeResult(group1 = n1Int, group2 = n2Int, total = n1Int + n2Int)
    }

    // 6. Two means, using effect size d and ratio r = n2/n1
    // d = (μ2 - μ1) / σ,  n1 ≥ ((1 + r)/r) (Z_{1-α/2} + Z_{1-β})² / d²
    fun compareTwoMeansEffectSize(
        alpha: Double,
        beta: Double,
        d: Double,
        r: Double
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val factor = (1.0 + r) / r
        val n1 = factor * (zAlpha + zBeta).pow(2.0) / d.pow(2.0)
        val n1Int = CommonMath.ceilInt(n1)
        val n2Int = CommonMath.ceilInt(r * n1Int)
        return SampleSizeResult(group1 = n1Int, group2 = n2Int, total = n1Int + n2Int)
    }

    // 7. Paired means (Before–After)
    // n ≥ 2 (Z_{1-α/2} + Z_{1-β})² (σ_d² / δ²)
    fun comparePairedMeans(
        alpha: Double,
        beta: Double,
        delta: Double,
        sigmaDiff: Double
    ): SampleSizeResult {
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val numerator = 2.0 * (zAlpha + zBeta).pow(2.0) * sigmaDiff.pow(2.0)
        val denom = delta.pow(2.0)
        val n = numerator / denom
        val nInt = CommonMath.ceilInt(n)
        return SampleSizeResult(total = nInt)
    }
}
