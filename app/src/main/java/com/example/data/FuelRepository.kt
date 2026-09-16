package com.example.data

import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlin.math.round

class FuelRepository(
    private val vehicleDao: VehicleDao,
    private val transactionDao: TransactionDao
) {
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getVehicle(id: Long): Flow<VehicleEntity?> = vehicleDao.getVehicleById(id)

    fun getTransactionsForVehicle(vehicleId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForVehicle(vehicleId)

    suspend fun checkAndApplyWeeklyResets() {
        val currentSunday = DateUtils.getCurrentWeekSundayEpoch()
        // Reset all vehicles whose last reset is older than the current week's Sunday
        vehicleDao.resetAllQuotas(currentSunday)
    }

    suspend fun addVehicle(
        vehicleNumber: String,
        vehicleType: String,
        fuelType: String,
        weeklyQuota: Double,
        qrPayload: String,
        isPrimary: Boolean = false
    ): Long {
        val currentSunday = DateUtils.getCurrentWeekSundayEpoch()
        val vehicle = VehicleEntity(
            vehicleNumber = vehicleNumber.trim().uppercase(),
            vehicleType = vehicleType,
            fuelType = fuelType,
            weeklyQuota = weeklyQuota,
            balanceQuota = weeklyQuota,
            qrPayload = qrPayload.ifBlank { "NFP:LK:${vehicleNumber.trim().uppercase()}:$vehicleType:$fuelType" },
            lastResetWeekSundayEpoch = currentSunday,
            isPrimary = isPrimary
        )
        val id = vehicleDao.insertVehicle(vehicle)
        if (isPrimary) {
            vehicleDao.setPrimaryVehicle(id)
        }
        return id
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun setPrimary(vehicleId: Long) {
        vehicleDao.setPrimaryVehicle(vehicleId)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) {
        transactionDao.deleteTransactionsForVehicle(vehicle.id)
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun resetQuota(vehicleId: Long) {
        val currentSunday = DateUtils.getCurrentWeekSundayEpoch()
        vehicleDao.resetVehicleQuota(vehicleId, currentSunday)
    }

    suspend fun pumpFuel(
        vehicleId: Long,
        litersToPump: Double,
        stationName: String = "Fuel Station"
    ): Result<Double> {
        val vehicle = vehicleDao.getVehicleByIdOnce(vehicleId)
            ?: return Result.failure(IllegalArgumentException("Vehicle not found"))

        if (litersToPump <= 0.0) {
            return Result.failure(IllegalArgumentException("Please enter a valid amount greater than 0"))
        }

        if (litersToPump > vehicle.balanceQuota) {
            return Result.failure(IllegalArgumentException("Amount exceeds remaining quota of ${DateUtils.formatLiters(vehicle.balanceQuota)}L"))
        }

        // Clean precision rounding to 1 decimal place to prevent IEEE 754 floating point bug
        val newBalance = round((vehicle.balanceQuota - litersToPump) * 10.0) / 10.0
        val updatedVehicle = vehicle.copy(balanceQuota = newBalance)
        vehicleDao.updateVehicle(updatedVehicle)

        val transaction = TransactionEntity(
            vehicleId = vehicle.id,
            vehicleNumber = vehicle.vehicleNumber,
            litersPumped = litersToPump,
            previousBalance = vehicle.balanceQuota,
            newBalance = newBalance,
            stationName = stationName.ifBlank { "Fuel Station" },
            fuelType = vehicle.fuelType,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)

        return Result.success(newBalance)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity, restoreQuota: Boolean = true) {
        if (restoreQuota) {
            val vehicle = vehicleDao.getVehicleByIdOnce(transaction.vehicleId)
            if (vehicle != null) {
                val restored = round((vehicle.balanceQuota + transaction.litersPumped) * 10.0) / 10.0
                val bounded = if (restored > vehicle.weeklyQuota) vehicle.weeklyQuota else restored
                vehicleDao.updateVehicle(vehicle.copy(balanceQuota = bounded))
            }
        }
        transactionDao.deleteTransaction(transaction)
    }
}
