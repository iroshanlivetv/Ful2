package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity
import com.example.ui.components.FuelTypeBadge
import com.example.ui.components.QuotaProgressBar
import com.example.ui.components.VehicleTypeIcon
import com.example.ui.theme.FuelAmberWarning
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.FuelRedAlert
import com.example.ui.theme.Slate400
import com.example.util.DateUtils

@Composable
fun WalletListScreen(
    vehicles: List<VehicleEntity>,
    onSelectVehicle: (Long) -> Unit,
    onAddVehicleClick: () -> Unit,
    onEditVehicleClick: (VehicleEntity) -> Unit,
    onDeleteVehicleClick: (VehicleEntity) -> Unit,
    onSetPrimaryClick: (Long) -> Unit,
    onQuickPumpClick: (VehicleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVehicleClick,
                containerColor = FuelBluePrimary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("wallet_fab_add_pass")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Pass")
            }
        }
    ) { innerPadding ->
        if (vehicles.isEmpty()) {
            EmptyDashboardState(
                onAddClick = onAddVehicleClick,
                onSeedDemoData = {},
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("wallet_list_screen"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                item {
                    val totalRemaining = vehicles.sumOf { it.balanceQuota }
                    val totalWeekly = vehicles.sumOf { it.weeklyQuota }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FuelBluePrimary
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "TOTAL WALLET QUOTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = DateUtils.formatLiters(totalRemaining),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "/ ${DateUtils.formatLiters(totalWeekly)} Liters Available",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${vehicles.size} registered vehicle passes in wallet",
                                fontSize = 12.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "REGISTERED PASSES (${vehicles.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                }

                items(vehicles, key = { it.id }) { vehicle ->
                    VehiclePassCard(
                        vehicle = vehicle,
                        onOpen = { onSelectVehicle(vehicle.id) },
                        onEdit = { onEditVehicleClick(vehicle) },
                        onDelete = { onDeleteVehicleClick(vehicle) },
                        onSetPrimary = { onSetPrimaryClick(vehicle.id) },
                        onQuickPump = { onQuickPumpClick(vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
fun VehiclePassCard(
    vehicle: VehicleEntity,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit,
    onQuickPump: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("vehicle_card_${vehicle.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (vehicle.isPrimary) BorderStroke(1.5.dp, FuelBluePrimary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VehicleTypeIcon(vehicleType = vehicle.vehicleType, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.vehicleNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            if (vehicle.isPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Primary",
                                    tint = FuelAmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        FuelTypeBadge(fuelType = vehicle.fuelType)
                    }
                }

                // Balance display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${DateUtils.formatLiters(vehicle.balanceQuota)} L",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (vehicle.balanceQuota < 5) FuelRedAlert else FuelBluePrimary
                    )
                    Text(
                        text = "of ${DateUtils.formatLiters(vehicle.weeklyQuota)}L",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            QuotaProgressBar(balance = vehicle.balanceQuota, weeklyQuota = vehicle.weeklyQuota)

            Spacer(modifier = Modifier.height(14.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Show QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onQuickPump,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pump", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSetPrimary) {
                        Icon(
                            if (vehicle.isPrimary) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Set Primary",
                            tint = if (vehicle.isPrimary) FuelAmberWarning else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FuelRedAlert.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
