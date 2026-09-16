package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.VehicleEntity
import com.example.ui.components.FuelTypeBadge
import com.example.ui.components.VehicleTypeIcon
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.FuelRedAlert
import com.example.ui.theme.Slate400
import com.example.util.DateUtils
import kotlin.math.round

@Composable
fun PumpFuelDialog(
    vehicle: VehicleEntity,
    onDismiss: () -> Unit,
    onConfirmPump: (liters: Double, station: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var stationName by remember { mutableStateOf("Ceypetco Station") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val resultingBalance = round((vehicle.balanceQuota - enteredAmount) * 10.0) / 10.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .testTag("pump_fuel_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VehicleTypeIcon(vehicleType = vehicle.vehicleType, size = 40)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = vehicle.vehicleNumber,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            FuelTypeBadge(fuelType = vehicle.fuelType)
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_pump_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Current balance display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FuelBluePrimary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AVAILABLE QUOTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FuelBluePrimary
                            )
                            Text(
                                text = "${DateUtils.formatLiters(vehicle.balanceQuota)} Liters",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (enteredAmount > 0 && resultingBalance >= 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "AFTER PUMP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400
                                )
                                Text(
                                    text = "${DateUtils.formatLiters(resultingBalance)} L",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FuelBluePrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Enter Liters text field
                Text(
                    text = "LITERS PUMPED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorText = null
                    },
                    placeholder = { Text("0.0") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pump_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("Liters", fontWeight = FontWeight.Bold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuelBluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets: 2L, 5L, 10L, 20L, Full Remaining
                val presets = listOf(2.0, 5.0, 10.0, 20.0).filter { it <= vehicle.balanceQuota }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    amountText = DateUtils.formatLiters(preset)
                                    errorText = null
                                }
                                .padding(vertical = 8.dp)
                                .testTag("preset_${preset.toInt()}L"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${preset.toInt()}L",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Max remaining button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FuelBluePrimary.copy(alpha = 0.15f))
                            .clickable {
                                amountText = DateUtils.formatLiters(vehicle.balanceQuota)
                                errorText = null
                            }
                            .padding(vertical = 8.dp)
                            .testTag("preset_max"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Full (${DateUtils.formatLiters(vehicle.balanceQuota)}L)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuelBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Station Name / Note
                Text(
                    text = "FUEL STATION / SHED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Slate400)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("station_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = FuelRedAlert,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Pump Button
                Button(
                    onClick = {
                        if (enteredAmount <= 0.0) {
                            errorText = "Please enter pumped liters"
                            return@Button
                        }
                        if (enteredAmount > vehicle.balanceQuota) {
                            errorText = "Cannot pump more than remaining quota (${DateUtils.formatLiters(vehicle.balanceQuota)}L)"
                            return@Button
                        }
                        onConfirmPump(enteredAmount, stationName.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_pump_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary)
                ) {
                    Text(
                        text = "CONFIRM & DEDUCT QUOTA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
