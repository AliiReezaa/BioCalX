package com.example.samplesizecalculator.domain.models

data class SingleMeanInput(
    val alpha: Double,   // Type I error (α)
    val sigma: Double,   // Standard deviation (σ)
    val d: Double        // Estimation error (d)
)
