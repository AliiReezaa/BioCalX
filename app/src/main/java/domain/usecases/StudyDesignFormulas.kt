package com.example.samplesizecalculator.domain.usecases

import com.example.samplesizecalculator.domain.models.SampleSizeResult
import kotlin.math.*

object StudyDesignFormulas {

    // Helper for case-control & cohort core n
    // p = (p1 + r p2) / (1 + r)
    // n =
    //  [ Z_{1-α/2}√((1+r)p(1-p)) + Z_{1-β}√(r p1(1-p1) + p2(1-p2)) ]² /
    //    [ r (p2 - p1)² ]
    private fun coreTwoGroupN(
        alpha: Double,
        beta: Double,
        p1: Double,
        p2: Double,
        r: Double
    ): Double {
        val p = (p1 + r * p2) / (1.0 + r)
        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val term1 = zAlpha * sqrt((1.0 + r) * p * (1.0 - p))
        val term2 = zBeta * sqrt(r * p1 * (1.0 - p1) + p2 * (1.0 - p2))
        return (term1 + term2).pow(2.0) / (r * (p2 - p1).pow(2.0))
    }

    // 18. Case-control study
    // p2 = p1 OR / [1 + p1 (OR - 1)]
    // n as in coreTwoGroupN
    // n_case ≥ n/4 [1 + √(1 + 2(r+1)/(n r |p2-p1|))]²
    fun caseControl(
        alpha: Double,
        beta: Double,
        p1: Double, // exposure in controls (non-disease)
        OR: Double,
        r: Double   // non-case / case
    ): SampleSizeResult {
        val p2 = p1 * OR / (1.0 + p1 * (OR - 1.0))
        val nCore = coreTwoGroupN(alpha, beta, p1, p2, r)

        val n = nCore
        val numerator = 1.0 + sqrt(1.0 + 2.0 * (r + 1.0) / (n * r * abs(p2 - p1)))
        val nCase = n / 4.0 * numerator.pow(2.0)
        val nCaseInt = CommonMath.ceilInt(nCase)
        val nControlInt = CommonMath.ceilInt(r * nCaseInt)
        return SampleSizeResult(group1 = nCaseInt, group2 = nControlInt, total = nCaseInt + nControlInt)
    }

    // 19. Cohort study
    // p2 = p1 RR
    // p = (p1 + r p2)/(1 + r)
    // same n and inflation to n_exposure as case-control
    fun cohort(
        alpha: Double,
        beta: Double,
        p1: Double, // risk in unexposed
        RR: Double,
        r: Double   // non-exposed / exposed
    ): SampleSizeResult {
        val p2 = p1 * RR
        val nCore = coreTwoGroupN(alpha, beta, p1, p2, r)

        val n = nCore
        val numerator = 1.0 + sqrt(1.0 + 2.0 * (r + 1.0) / (n * r * abs(p2 - p1)))
        val nExp = n / 4.0 * numerator.pow(2.0)
        val nExpInt = CommonMath.ceilInt(nExp)
        val nUnexpInt = CommonMath.ceilInt(r * nExpInt)
        return SampleSizeResult(group1 = nExpInt, group2 = nUnexpInt, total = nExpInt + nUnexpInt)
    }

    // 20. Survival study
    // n_event ≥ 4 (Z_{1-α/2} + Z_{1-β})² / (ln(HR))²
    // n_total = n_event / p_event,  n1 = n2 = n_total / 2
    fun survival(
        alpha: Double,
        beta: Double,
        HR: Double,
        pEvent: Double
    ): SampleSizeResult {
        require(HR > 0.0 && HR != 1.0) { "Hazard ratio (HR) must be > 0 and ≠ 1." }
        require(pEvent > 0.0 && pEvent < 1.0) { "Event proportion (p) must be between 0 and 1." }

        val zAlpha = CommonMath.zAlphaTwoSided(alpha)
        val zBeta = CommonMath.zBeta(beta)
        val nEvents = 4.0 * (zAlpha + zBeta).pow(2.0) / ln(HR).pow(2.0)
        val nEventsInt = CommonMath.ceilInt(nEvents)

        val nTotal = nEvents / pEvent
        val nTotalInt = CommonMath.ceilInt(nTotal)

        val perGroup = nTotal / 2.0
        val perGroupInt = CommonMath.ceilInt(perGroup)

        return SampleSizeResult(
            eventCount = nEventsInt,
            group1 = perGroupInt,
            group2 = perGroupInt,
            total = nTotalInt
        )
    }
}
