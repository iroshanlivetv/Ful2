package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val vehicleNumber: String,
    val litersPumped: Double,
    val previousBalance: Double,
    val newBalance: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val stationName: String = "Fuel Station",
    val fuelType: String = "Petrol 92"
)
