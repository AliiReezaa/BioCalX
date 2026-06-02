package com.example.samplesizecalculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CalculationEntity): Long

    @Query("SELECT * FROM calculation_history ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<CalculationEntity>>

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
