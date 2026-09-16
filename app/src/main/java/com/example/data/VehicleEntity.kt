package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleNumber: String,
    val vehicleType: String, // "CAR", "BIKE", "VAN", "TUK"
    val fuelType: String = "Petrol 92", // "Petrol 92", "Petrol 95", "Auto Diesel", "Super Diesel"
    val weeklyQuota: Double,
    val balanceQuota: Double,
    val qrPayload: String,
    val lastResetWeekSundayEpoch: Long = 0,
    val isPrimary: Boolean = false,
    val createdAtEpoch: Long = System.currentTimeMillis()
)
