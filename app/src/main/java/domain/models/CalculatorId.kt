package com.example.samplesizecalculator.domain.models

enum class CalculatorId {
    SINGLE_MEAN,
    SINGLE_MEAN_FINITE,
    COMPARE_TWO_MEANS_EQUAL,
    COMPARE_MORE_THAN_TWO_MEANS_EQUAL,
    COMPARE_TWO_MEANS_UNEQUAL,
    COMPARE_TWO_MEANS_EFFECT,
    COMPARE_PAIRED_MEANS,
    SINGLE_PROPORTION,
    SINGLE_PROPORTION_FINITE,
    COMPARE_TWO_PROPORTIONS_EQUAL,
    COMPARE_MORE_THAN_TWO_PROPORTIONS_EQUAL,
    COMPARE_TWO_PROPORTIONS_UNEQUAL,
    COMPARE_PAIRED_PROPORTIONS,
    CORRELATION_COEFFICIENT,
    ESTIMATE_SENSITIVITY,
    ESTIMATE_SPECIFICITY,
    AREA_UNDER_ROC,
    CASE_CONTROL,
    COHORT,
    SURVIVAL
}

/**
 * Generic container for sample size results across calculators.
 */
data class SampleSizeResult(
    val group1: Int? = null,   // For two-group designs (n1, or base n)
    val group2: Int? = null,   // n2 where applicable
    val total: Int? = null,    // total sample size
    val eventCount: Int? = null // for survival designs (number of events)
)
