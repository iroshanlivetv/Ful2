package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TransactionEntity
import com.example.data.VehicleEntity
import com.example.ui.components.FuelTypeBadge
import com.example.ui.components.QuotaProgressBar
import com.example.ui.components.ScanStatusBadge
import com.example.ui.components.VehicleTypeIcon
import com.example.ui.theme.FuelAmberWarning
import com.example.ui.theme.FuelBlueDark
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.FuelGreenAccent
import com.example.ui.theme.FuelNavyBackground
import com.example.ui.theme.FuelRedAlert
import com.example.ui.theme.Slate400
import com.example.util.DateUtils
import kotlin.math.round

@Composable
fun DashboardScreen(
    vehicles: List<VehicleEntity>,
    selectedVehicle: VehicleEntity?,
    qrBitmap: Bitmap?,
    daysUntilReset: Int,
    onSelectVehicle: (Long) -> Unit,
    onAddVehicleClick: () -> Unit,
    onPumpClick: () -> Unit,
    onEditVehicleClick: (VehicleEntity) -> Unit,
    onFullscreenQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (vehicles.isEmpty()) {
        EmptyDashboardState(
            onAddClick = onAddVehicleClick,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Horizontal Vehicle Selector Strip
        item {
            Column {
                Text(
                    text = "SELECT VEHICLE PASS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FuelBluePrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        val isSelected = vehicle.id == selectedVehicle?.id
                        VehicleSelectorChip(
                            vehicle = vehicle,
                            isSelected = isSelected,
                            onClick = { onSelectVehicle(vehicle.id) }
                        )
                    }

                    item {
                        Surface(
                            onClick = onAddVehicleClick,
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("quick_add_pass_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Pass",
                                    tint = FuelBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Pass",
                                    color = FuelBluePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Vehicle Pass Card (Hero)
        if (selectedVehicle != null) {
            item {
                ActivePassHeroCard(
                    vehicle = selectedVehicle,
                    qrBitmap = qrBitmap,
                    daysUntilReset = daysUntilReset,
                    onPumpClick = onPumpClick,
                    onEditClick = { onEditVehicleClick(selectedVehicle) },
                    onFullscreenQrClick = onFullscreenQrClick
                )
            }

            // Quick Stats Bar
            item {
                QuickStatsCard(
                    vehicle = selectedVehicle,
                    daysUntilReset = daysUntilReset
                )
            }
        }
    }
}


@Composable
fun VehicleSelectorChip(
    vehicle: VehicleEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) FuelBluePrimary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (isSelected) Color.White.copy(alpha = 0.85f) else Slate400

    Surface(
        onClick = onClick,
        modifier = Modifier.testTag("vehicle_chip_${vehicle.id}"),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val drawableRes = when (vehicle.vehicleType.uppercase()) {
                "BIKE" -> R.drawable.ic_vehicle_bike
                "TUK", "3WHEEL" -> R.drawable.ic_vehicle_tuk
                "VAN" -> R.drawable.ic_vehicle_van
                else -> R.drawable.ic_vehicle_car
            }
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = vehicle.vehicleType,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = vehicle.vehicleNumber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "${DateUtils.formatLiters(vehicle.balanceQuota)}L left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = subColor
                )
            }
        }
    }
}


@Composable
fun ActivePassHeroCard(
    vehicle: VehicleEntity,
    qrBitmap: Bitmap?,
    daysUntilReset: Int,
    onPumpClick: () -> Unit,
    onEditClick: () -> Unit,
    onFullscreenQrClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_pass_hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vehicle Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VehicleTypeIcon(vehicleType = vehicle.vehicleType, size = 48)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.vehicleNumber,
                                style = MaterialTheme.typography.titleLarge,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = vehicle.vehicleType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )
                            Text(text = "•", color = Slate400)
                            FuelTypeBadge(fuelType = vehicle.fuelType)
                        }
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_active_vehicle_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Pass", tint = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // QR Code with tap to enlarge
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(1f)
                    .clickable(onClick = onFullscreenQrClick)
                    .testTag("dashboard_qr_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Fuel Pass QR",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Slate400
                        )
                    }

                    // Enlarge badge on top corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(FuelBluePrimary.copy(alpha = 0.9f))
                            .padding(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            ScanStatusBadge()

            Spacer(modifier = Modifier.height(20.dp))

            // Quota Balance Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE QUOTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FuelBluePrimary,
                            letterSpacing = 0.8.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = DateUtils.formatLiters(vehicle.balanceQuota),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = if (vehicle.balanceQuota < 5) FuelRedAlert else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Liters",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "WEEKLY LIMIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400
                        )
                        Text(
                            text = "${DateUtils.formatLiters(vehicle.weeklyQuota)} L",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                QuotaProgressBar(
                    balance = vehicle.balanceQuota,
                    weeklyQuota = vehicle.weeklyQuota
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val usedLiters = round((vehicle.weeklyQuota - vehicle.balanceQuota) * 10.0) / 10.0
                    Text(
                        text = "Used: ${DateUtils.formatLiters(usedLiters)}L",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                    Text(
                        text = "Renews Sunday midnight ($daysUntilReset d)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FuelBluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Management Action: Pump Fuel
            Button(
                onClick = onPumpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("pump_fuel_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary)
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PUMP FUEL",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }

        }
    }
}

@Composable
fun QuickStatsCard(
    vehicle: VehicleEntity,
    daysUntilReset: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REMAINING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${DateUtils.formatLiters(vehicle.balanceQuota)}L",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = FuelBluePrimary
                )
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "USED THIS CYCLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.8.sp
                )
                val used = round((vehicle.weeklyQuota - vehicle.balanceQuota) * 10.0) / 10.0
                Text(
                    text = "${DateUtils.formatLiters(used)}L",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CYCLE ENDS IN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "$daysUntilReset Days",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = FuelGreenAccent
                )
            }
        }
    }
}

@Composable
fun EmptyDashboardState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "FuelPass Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Fuel Passes Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your Sri Lanka National Fuel Pass to track your weekly quota and access your QR anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("empty_add_pass_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADD YOUR VEHICLE PASS", fontWeight = FontWeight.Bold)
            }
        }
    }
}
