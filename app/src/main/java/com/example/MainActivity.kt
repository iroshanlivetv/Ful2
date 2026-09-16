package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VehicleEntity
import com.example.ui.FuelNavTab
import com.example.ui.FuelViewModel
import com.example.ui.dialogs.AddEditVehicleDialog
import com.example.ui.dialogs.FullscreenQrDialog
import com.example.ui.dialogs.PumpFuelDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.WalletListScreen
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.FuelPassTheme
import com.example.ui.theme.Slate400
import com.example.util.DateUtils
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: FuelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            FuelPassTheme(darkTheme = isDarkMode) {
                FuelPassApp(viewModel = viewModel, isDarkMode = isDarkMode)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPassApp(viewModel: FuelViewModel, isDarkMode: Boolean) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showAddEditDialog by viewModel.showAddVehicleDialog.collectAsStateWithLifecycle()
    val vehicleToEdit by viewModel.vehicleToEdit.collectAsStateWithLifecycle()
    val showPumpDialog by viewModel.showPumpDialog.collectAsStateWithLifecycle()
    val showFullscreenQr by viewModel.showFullscreenQr.collectAsStateWithLifecycle()
    val vehiclePendingDelete by viewModel.vehiclePendingDelete.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Seed defaults on initial launch if empty
    LaunchedEffect(uiState.isInitialized) {
        if (uiState.isInitialized && uiState.vehicles.isEmpty()) {
            viewModel.seedDefaultIfEmpty()
        }
    }

    // Listen for toast/snackbar events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_top_bar")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "FuelPass Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "FuelPass",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Digital Fuel Wallet",
                                fontSize = 10.sp,
                                color = Slate400,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (uiState.currentTab != FuelNavTab.HISTORY) {
                        IconButton(
                            onClick = { viewModel.openAddVehicleDialog() },
                            modifier = Modifier.testTag("top_bar_add_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Pass",
                                tint = FuelBluePrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == FuelNavTab.DASHBOARD,
                    onClick = { viewModel.selectTab(FuelNavTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = FuelBluePrimary.copy(alpha = 0.15f),
                        selectedIconColor = FuelBluePrimary,
                        selectedTextColor = FuelBluePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == FuelNavTab.PASSES,
                    onClick = { viewModel.selectTab(FuelNavTab.PASSES) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "My Passes") },
                    label = { Text("My Passes", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = FuelBluePrimary.copy(alpha = 0.15f),
                        selectedIconColor = FuelBluePrimary,
                        selectedTextColor = FuelBluePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_passes")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == FuelNavTab.HISTORY,
                    onClick = { viewModel.selectTab(FuelNavTab.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Fuel Logs") },
                    label = { Text("Logs", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = FuelBluePrimary.copy(alpha = 0.15f),
                        selectedIconColor = FuelBluePrimary,
                        selectedTextColor = FuelBluePrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding),
            label = "tab_content_transition"
        ) { tab ->
            when (tab) {
                FuelNavTab.DASHBOARD -> {
                    DashboardScreen(
                        vehicles = uiState.vehicles,
                        selectedVehicle = uiState.selectedVehicle,
                        qrBitmap = uiState.selectedQrBitmap,
                        daysUntilReset = uiState.daysUntilReset,
                        onSelectVehicle = { viewModel.selectVehicle(it) },
                        onAddVehicleClick = { viewModel.openAddVehicleDialog() },
                        onPumpClick = { viewModel.openPumpDialog() },
                        onEditVehicleClick = { viewModel.openEditVehicleDialog(it) },
                        onFullscreenQrClick = { viewModel.showFullscreenQr.value = true },
                        onSeedDemoData = { viewModel.seedDefaultIfEmpty() }
                    )
                }

                FuelNavTab.PASSES -> {
                    WalletListScreen(
                        vehicles = uiState.vehicles,
                        onSelectVehicle = { id ->
                            viewModel.selectVehicle(id)
                            viewModel.selectTab(FuelNavTab.DASHBOARD)
                        },
                        onAddVehicleClick = { viewModel.openAddVehicleDialog() },
                        onEditVehicleClick = { viewModel.openEditVehicleDialog(it) },
                        onDeleteVehicleClick = { viewModel.requestDeleteVehicle(it) },
                        onSetPrimaryClick = { viewModel.setAsPrimary(it) },
                        onQuickPumpClick = { vehicle ->
                            viewModel.selectVehicle(vehicle.id)
                            viewModel.openPumpDialog()
                        }
                    )
                }

                FuelNavTab.HISTORY -> {
                    HistoryScreen(
                        transactions = uiState.allTransactions,
                        onUndoTransaction = { viewModel.deleteTransaction(it) }
                    )
                }
            }
        }
    }

    // Add or Edit Vehicle Dialog
    if (showAddEditDialog) {
        AddEditVehicleDialog(
            initialVehicle = vehicleToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, vNo, vType, fType, quota, qr, isPrim ->
                viewModel.saveVehicle(id, vNo, vType, fType, quota, qr, isPrim)
            }
        )
    }

    // Pump Fuel Dialog
    if (showPumpDialog && uiState.selectedVehicle != null) {
        PumpFuelDialog(
            vehicle = uiState.selectedVehicle!!,
            onDismiss = { viewModel.closePumpDialog() },
            onConfirmPump = { liters, station ->
                viewModel.pumpFuel(liters, station)
            }
        )
    }

    // Fullscreen QR Modal
    if (showFullscreenQr && uiState.selectedVehicle != null) {
        FullscreenQrDialog(
            vehicle = uiState.selectedVehicle!!,
            qrBitmap = uiState.selectedQrBitmap,
            onDismiss = { viewModel.showFullscreenQr.value = false }
        )
    }

    // Confirm Delete Dialog
    if (vehiclePendingDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteVehicle() },
            title = { Text("Delete Vehicle Pass?") },
            text = {
                Text("Are you sure you want to remove ${vehiclePendingDelete?.vehicleNumber} from your FuelPass wallet? All logged fuel history for this vehicle will also be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteVehicle() },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteVehicle() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

