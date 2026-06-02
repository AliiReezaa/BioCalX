package com.example.samplesizecalculator.domain.repositories

import com.example.samplesizecalculator.data.local.CalculationEntity
import kotlinx.coroutines.flow.Flow

interface CalculationHistoryRepository {
    fun observeAll(): Flow<List<CalculationEntity>>
    suspend fun save(calculationTitle: String, inputsText: String, resultsText: String): Long
    suspend fun delete(id: Long)
    suspend fun clearAll()
}
