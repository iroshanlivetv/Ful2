package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VehicleEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FuelDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: FuelDatabase? = null

        fun getInstance(context: Context): FuelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FuelDatabase::class.java,
                    "fuel_wallet.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
