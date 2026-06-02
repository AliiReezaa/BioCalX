package com.example.samplesizecalculator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalculationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BioCalXDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile private var INSTANCE: BioCalXDatabase? = null

        fun getInstance(context: Context): BioCalXDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BioCalXDatabase::class.java,
                    "biocalx.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
