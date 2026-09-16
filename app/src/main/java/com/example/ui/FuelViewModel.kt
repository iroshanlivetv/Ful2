package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FuelDatabase
import com.example.data.FuelRepository
import com.example.data.TransactionEntity
import com.example.data.VehicleEntity
import com.example.util.DateUtils
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FuelNavTab {
    DASHBOARD,
    PASSES,
    HISTORY
}

data class FuelUiState(
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicleId: Long? = null,
    val selectedVehicle: VehicleEntity? = null,
    val selectedVehicleTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val selectedQrBitmap: Bitmap? = null,
    val currentTab: FuelNavTab = FuelNavTab.DASHBOARD,
    val daysUntilReset: Int = 0,
    val isInitialized: Boolean = false
)

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FuelRepository

    init {
        val database = FuelDatabase.getInstance(application)
        repository = FuelRepository(database.vehicleDao(), database.transactionDao())

        // Seed initial sample data if the wallet is empty so the user can immediately test
        viewModelScope.launch {
            repository.checkAndApplyWeeklyResets()
        }
    }

    private val _selectedVehicleId = MutableStateFlow<Long?>(null)
    private val _currentTab = MutableStateFlow(FuelNavTab.DASHBOARD)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val prefs = application.getSharedPreferences("fuelpass_prefs", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("key_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        prefs.edit().putBoolean("key_dark_mode", newMode).apply()
    }

    // Dialog state holders
    val showAddVehicleDialog = MutableStateFlow(false)
    val vehicleToEdit = MutableStateFlow<VehicleEntity?>(null)
    val showPumpDialog = MutableStateFlow(false)
    val showFullscreenQr = MutableStateFlow(false)
    val vehiclePendingDelete = MutableStateFlow<VehicleEntity?>(null)

    val uiState: StateFlow<FuelUiState> = combine(
        repository.allVehicles,
        repository.allTransactions,
        _selectedVehicleId,
        _currentTab
    ) { vehicles, transactions, selectedId, currentTab ->
        // Auto-select primary or first vehicle if none selected or selected was deleted
        val activeVehicle = when {
            selectedId != null -> vehicles.find { it.id == selectedId } ?: vehicles.firstOrNull()
            else -> vehicles.find { it.isPrimary } ?: vehicles.firstOrNull()
        }

        val qrBitmap = activeVehicle?.let { vehicle ->
            QrCodeGenerator.generateQrBitmap(vehicle.qrPayload, size = 600)
        }

        val selectedVehicleTx = if (activeVehicle != null) {
            transactions.filter { it.vehicleId == activeVehicle.id }
        } else emptyList()

        FuelUiState(
            vehicles = vehicles,
            selectedVehicleId = activeVehicle?.id,
            selectedVehicle = activeVehicle,
            selectedVehicleTransactions = selectedVehicleTx,
            allTransactions = transactions,
            selectedQrBitmap = qrBitmap,
            currentTab = currentTab,
            daysUntilReset = DateUtils.getDaysUntilNextReset(),
            isInitialized = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FuelUiState(daysUntilReset = DateUtils.getDaysUntilNextReset())
    )

    fun selectVehicle(vehicleId: Long) {
        _selectedVehicleId.value = vehicleId
    }

    fun selectTab(tab: FuelNavTab) {
        _currentTab.value = tab
    }

    fun openAddVehicleDialog() {
        vehicleToEdit.value = null
        showAddVehicleDialog.value = true
    }

    fun openEditVehicleDialog(vehicle: VehicleEntity) {
        vehicleToEdit.value = vehicle
        showAddVehicleDialog.value = true
    }

    fun closeAddEditDialog() {
        showAddVehicleDialog.value = false
        vehicleToEdit.value = null
    }

    fun saveVehicle(
        id: Long?,
        vehicleNumber: String,
        vehicleType: String,
        fuelType: String,
        weeklyQuota: Double,
        qrPayload: String,
        isPrimary: Boolean
    ) {
        viewModelScope.launch {
            if (id == null || id == 0L) {
                val newId = repository.addVehicle(
                    vehicleNumber = vehicleNumber,
                    vehicleType = vehicleType,
                    fuelType = fuelType,
                    weeklyQuota = weeklyQuota,
                    qrPayload = qrPayload,
                    isPrimary = isPrimary
                )
                _selectedVehicleId.value = newId
                _toastEvent.emit("Vehicle $vehicleNumber added to wallet!")
            } else {
                val current = uiState.value.vehicles.find { it.id == id }
                if (current != null) {
                    val updated = current.copy(
                        vehicleNumber = vehicleNumber.trim().uppercase(),
                        vehicleType = vehicleType,
                        fuelType = fuelType,
                        weeklyQuota = weeklyQuota,
                        // If quota increased or changed, ensure balance doesn't exceed new quota
                        balanceQuota = if (current.balanceQuota > weeklyQuota) weeklyQuota else current.balanceQuota,
                        qrPayload = qrPayload.ifBlank { current.qrPayload },
                        isPrimary = isPrimary
                    )
                    repository.updateVehicle(updated)
                    if (isPrimary) {
                        repository.setPrimary(updated.id)
                    }
                    _toastEvent.emit("Vehicle $vehicleNumber updated!")
                }
            }
            closeAddEditDialog()
        }
    }

    fun openPumpDialog() {
        if (uiState.value.selectedVehicle == null) {
            viewModelScope.launch { _toastEvent.emit("Please add or select a vehicle first") }
            return
        }
        showPumpDialog.value = true
    }

    fun closePumpDialog() {
        showPumpDialog.value = false
    }

    fun pumpFuel(liters: Double, stationName: String) {
        val vehicle = uiState.value.selectedVehicle ?: return
        viewModelScope.launch {
            val result = repository.pumpFuel(vehicle.id, liters, stationName)
            result.onSuccess { newBalance ->
                showPumpDialog.value = false
                _toastEvent.emit("Pumped ${DateUtils.formatLiters(liters)}L! Remaining: ${DateUtils.formatLiters(newBalance)}L")
            }.onFailure { error ->
                _toastEvent.emit(error.message ?: "Failed to log pump")
            }
        }
    }

    fun resetQuotaForActiveVehicle() {
        val vehicle = uiState.value.selectedVehicle ?: return
        viewModelScope.launch {
            repository.resetQuota(vehicle.id)
            _toastEvent.emit("Weekly quota reset to ${DateUtils.formatLiters(vehicle.weeklyQuota)}L for ${vehicle.vehicleNumber}")
        }
    }

    fun requestDeleteVehicle(vehicle: VehicleEntity) {
        vehiclePendingDelete.value = vehicle
    }

    fun confirmDeleteVehicle() {
        val vehicle = vehiclePendingDelete.value ?: return
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            vehiclePendingDelete.value = null
            _toastEvent.emit("Vehicle ${vehicle.vehicleNumber} removed from wallet")
        }
    }

    fun cancelDeleteVehicle() {
        vehiclePendingDelete.value = null
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction, restoreQuota = true)
            _toastEvent.emit("Pump log undone and ${DateUtils.formatLiters(transaction.litersPumped)}L quota restored")
        }
    }

    fun setAsPrimary(vehicleId: Long) {
        viewModelScope.launch {
            repository.setPrimary(vehicleId)
            _toastEvent.emit("Set as primary vehicle")
        }
    }

    fun seedDefaultIfEmpty() {
        viewModelScope.launch {
            val vehicles = uiState.value.vehicles
            if (vehicles.isEmpty()) {
                val carId = repository.addVehicle(
                    vehicleNumber = "CAS-1234",
                    vehicleType = "CAR",
                    fuelType = "Petrol 92",
                    weeklyQuota = 25.0,
                    qrPayload = "NFP:LK:CAS-1234:CAR:Petrol 92",
                    isPrimary = true
                )
                repository.addVehicle(
                    vehicleNumber = "BI-5678",
                    vehicleType = "BIKE",
                    fuelType = "Petrol 95",
                    weeklyQuota = 8.0,
                    qrPayload = "NFP:LK:BI-5678:BIKE:Petrol 95",
                    isPrimary = false
                )
                repository.addVehicle(
                    vehicleNumber = "WP-AB-9012",
                    vehicleType = "TUK",
                    fuelType = "Petrol 92",
                    weeklyQuota = 20.0,
                    qrPayload = "NFP:LK:WP-AB-9012:TUK:Petrol 92",
                    isPrimary = false
                )
                _selectedVehicleId.value = carId
            }
        }
    }
}
