package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY isPrimary DESC, createdAtEpoch DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: Long): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleByIdOnce(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE vehicles SET balanceQuota = weeklyQuota, lastResetWeekSundayEpoch = :sundayEpoch")
    suspend fun resetAllQuotas(sundayEpoch: Long)

    @Query("UPDATE vehicles SET balanceQuota = weeklyQuota, lastResetWeekSundayEpoch = :sundayEpoch WHERE id = :id")
    suspend fun resetVehicleQuota(id: Long, sundayEpoch: Long)

    @Query("UPDATE vehicles SET isPrimary = CASE WHEN id = :primaryId THEN 1 ELSE 0 END")
    suspend fun setPrimaryVehicle(primaryId: Long)

    @Query("SELECT * FROM vehicles WHERE vehicleNumber IN (:numbers)")
    suspend fun getVehiclesByNumbers(numbers: List<String>): List<VehicleEntity>

    @Query("DELETE FROM vehicles WHERE vehicleNumber IN (:numbers)")
    suspend fun deleteByVehicleNumbers(numbers: List<String>)

    @Query("DELETE FROM vehicles")
    suspend fun deleteAllVehicles()
}
