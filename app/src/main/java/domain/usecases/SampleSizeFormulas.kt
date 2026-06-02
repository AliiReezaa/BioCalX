package com.example.samplesizecalculator.domain.usecases

// ---------- Single proportion n ≥ Z^2 * p(1-p) / d^2 ----------

fun calculateSingleProportionSampleSize(
    alpha: Double,
    p: Double,
    d: Double
): Double {
    val z = CommonMath.zAlphaTwoSided(alpha)
    return (z * z) * p * (1.0 - p) / (d * d)
}

// ---------- Single mean n = (Z * σ / d)^2 ----------

fun calculateSingleMeanSampleSize(
    alpha: Double,
    sigma: Double,
    d: Double
): Double {
    val z = CommonMath.zAlphaTwoSided(alpha)
    val term = z * sigma / d
    return term * term
}

// ---------- Single mean in finite population ----------
// n = (Z * σ / d)^2
// n_total = Nn / (N + n - 1)

fun calculateSingleMeanFinitePopulationSampleSize(
    alpha: Double,
    sigma: Double,
    d: Double,
    populationSize: Double
): Pair<Double, Double> {
    val nInfinite = calculateSingleMeanSampleSize(alpha, sigma, d)
    val nFinite = (populationSize * nInfinite) / (populationSize + nInfinite - 1.0)
    return nInfinite to nFinite
}
