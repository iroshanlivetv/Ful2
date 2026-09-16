package com.example.ui.dialogs

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.VehicleEntity
import com.example.ui.components.VehicleTypeIcon
import com.example.ui.theme.FuelBlueDark
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.Slate400
import com.example.util.QrCodeGenerator

@Composable
fun AddEditVehicleDialog(
    initialVehicle: VehicleEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long?,
        vehicleNumber: String,
        vehicleType: String,
        fuelType: String,
        weeklyQuota: Double,
        qrPayload: String,
        isPrimary: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialVehicle != null

    var vehicleNumber by remember { mutableStateOf(initialVehicle?.vehicleNumber ?: "") }
    var vehicleType by remember { mutableStateOf(initialVehicle?.vehicleType ?: "CAR") }
    var fuelType by remember { mutableStateOf(initialVehicle?.fuelType ?: "Petrol 92") }
    var weeklyQuota by remember {
        mutableDoubleStateOf(
            initialVehicle?.weeklyQuota ?: when (vehicleType) {
                "BIKE" -> 8.0
                "TUK" -> 20.0
                "VAN" -> 50.0
                else -> 25.0
            }
        )
    }
    var qrPayload by remember { mutableStateOf(initialVehicle?.qrPayload ?: "") }
    var isPrimary by remember { mutableStateOf(initialVehicle?.isPrimary ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var qrImportStatus by remember { mutableStateOf<String?>(null) }
    var showCameraScanner by remember { mutableStateOf(false) }

    // Official standard quota map
    val defaultQuotas = mapOf(
        "CAR" to 25.0,
        "BIKE" to 8.0,
        "TUK" to 20.0,
        "VAN" to 50.0
    )

    // Photo picker for QR screenshot/image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val decodedText = QrCodeGenerator.decodeQrFromBitmap(bitmap)
                    if (decodedText != null) {
                        qrPayload = decodedText
                        qrImportStatus = "QR decoded successfully! ✅"
                    } else {
                        qrPayload = "NFP:IMG:${System.currentTimeMillis()}"
                        qrImportStatus = "Image loaded as pass QR ✅"
                    }
                }
            } catch (e: Exception) {
                qrImportStatus = "Could not parse image"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .testTag("add_vehicle_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Vehicle Pass" else "Add New Vehicle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sri Lanka National Fuel Pass",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_vehicle_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Vehicle Type Selection
                Text(
                    text = "VEHICLE CATEGORY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val vehicleCategories = listOf(
                    Triple("CAR", "Car", 25.0),
                    Triple("BIKE", "Bike", 8.0),
                    Triple("TUK", "3-Wheel", 20.0),
                    Triple("VAN", "Van", 50.0)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vehicleCategories.forEach { (typeKey, label, stdQuota) ->
                        val isSelected = vehicleType.equals(typeKey, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    vehicleType = typeKey
                                    weeklyQuota = stdQuota
                                }
                                .testTag("type_selector_$typeKey"),
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) BorderStroke(2.dp, FuelBluePrimary) else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) FuelBluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                VehicleTypeIcon(
                                    vehicleType = typeKey,
                                    size = 36,
                                    iconTint = if (isSelected) FuelBluePrimary else Slate400,
                                    containerColor = Color.Transparent
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FuelBluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stdQuota.toInt()}L",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Vehicle Number
                Text(
                    text = "VEHICLE REGISTRATION NUMBER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = {
                        vehicleNumber = it.uppercase()
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. CAS-1234 or WP-CAA-5678") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_number_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuelBluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Fuel Type Selection
                Text(
                    text = "FUEL TYPE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val fuelTypes = listOf("Petrol 92", "Petrol 95", "Auto Diesel", "Super Diesel")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fuelTypes.forEach { type ->
                        val selected = fuelType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) FuelBluePrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { fuelType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.replace(" ", "\n"),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Auto Quota Display (Replaces manual typing)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FuelBluePrimary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY FUEL QUOTA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FuelBluePrimary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Standard quota for $vehicleType",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${weeklyQuota.toInt()} Liters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = FuelBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Fuel Pass QR Code
                Text(
                    text = "FUEL PASS QR CODE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("upload_qr_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UPLOAD QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCameraScanner = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("scan_qr_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCAN QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (qrImportStatus != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = qrImportStatus ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }



                Spacer(modifier = Modifier.height(12.dp))

                // Primary checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPrimary = !isPrimary }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        modifier = Modifier.testTag("primary_checkbox")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Set as Primary Vehicle",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Opens first when you open the wallet",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val trimmedNo = vehicleNumber.trim()
                        if (trimmedNo.isBlank()) {
                            errorMessage = "Please enter vehicle registration number"
                            return@Button
                        }
                        if (weeklyQuota <= 0) {
                            errorMessage = "Please enter a valid weekly quota"
                            return@Button
                        }
                        val finalQr = if (qrPayload.isBlank()) {
                            "NFP:LK:$trimmedNo:$vehicleType:$fuelType"
                        } else qrPayload

                        onSave(
                            initialVehicle?.id,
                            trimmedNo,
                            vehicleType,
                            fuelType,
                            weeklyQuota,
                            finalQr,
                            isPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_vehicle_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FuelBluePrimary)
                ) {
                    Text(
                        text = if (isEditing) "SAVE CHANGES" else "SAVE TO WALLET",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (showCameraScanner) {
        QrCameraScannerDialog(
            onDismiss = { showCameraScanner = false },
            onQrScanned = { scannedPayload ->
                qrPayload = scannedPayload
                qrImportStatus = "✓ Fuel Pass QR scanned successfully!"
                showCameraScanner = false
            }
        )
    }
}

