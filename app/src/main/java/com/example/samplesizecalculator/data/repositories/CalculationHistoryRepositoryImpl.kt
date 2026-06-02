package com.example.samplesizecalculator.data.repositories

import android.content.Context
import com.example.samplesizecalculator.data.local.BioCalXDatabase
import com.example.samplesizecalculator.data.local.CalculationEntity
import com.example.samplesizecalculator.domain.repositories.CalculationHistoryRepository
import kotlinx.coroutines.flow.Flow

class CalculationHistoryRepositoryImpl(
    context: Context
) : CalculationHistoryRepository {

    private val dao = BioCalXDatabase.getInstance(context).calculationDao()

    override fun observeAll(): Flow<List<CalculationEntity>> = dao.observeAll()

    override suspend fun save(
        calculationTitle: String,
        inputsText: String,
        resultsText: String
    ): Long {
        return dao.insert(
            CalculationEntity(
                calculatorTitle = calculationTitle,
                inputsText = inputsText,
                resultsText = resultsText
            )
        )
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
