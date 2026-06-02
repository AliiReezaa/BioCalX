package com.example.samplesizecalculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val calculatorTitle: String,
    val inputsText: String,     // already formatted (human-readable)
    val resultsText: String,    // already formatted (human-readable)

    val createdAtEpochMs: Long = System.currentTimeMillis()
)
