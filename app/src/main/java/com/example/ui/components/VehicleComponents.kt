package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FuelAmberWarning
import com.example.ui.theme.FuelBluePrimary
import com.example.ui.theme.FuelGreenAccent
import com.example.ui.theme.FuelRedAlert
import com.example.ui.theme.Slate400
import com.example.util.DateUtils

@Composable
fun VehicleTypeIcon(
    vehicleType: String,
    modifier: Modifier = Modifier,
    iconTint: Color = FuelBluePrimary,
    containerColor: Color = FuelBluePrimary.copy(alpha = 0.12f),
    size: Int = 44
) {
    val drawableRes = when (vehicleType.uppercase()) {
        "BIKE" -> R.drawable.ic_vehicle_bike
        "TUK", "3WHEEL" -> R.drawable.ic_vehicle_tuk
        "VAN" -> R.drawable.ic_vehicle_van
        else -> R.drawable.ic_vehicle_car
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(containerColor)
            .testTag("vehicle_icon_${vehicleType.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = vehicleType,
            tint = iconTint,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun QuotaProgressBar(
    balance: Double,
    weeklyQuota: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (weeklyQuota > 0) (balance / weeklyQuota).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "quota_progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            progress > 0.25f -> FuelBluePrimary
            progress > 0.10f -> FuelAmberWarning
            else -> FuelRedAlert
        },
        label = "quota_color"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .testTag("quota_progress_bar"),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ScanStatusBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(FuelGreenAccent.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("scan_status_badge"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(FuelGreenAccent)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "READY FOR SCAN",
            color = FuelGreenAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun FuelTypeBadge(
    fuelType: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        fuelType.contains("95") -> Color(0xFF7C3AED).copy(alpha = 0.12f) to Color(0xFF7C3AED)
        fuelType.contains("Diesel") -> Color(0xFFEA580C).copy(alpha = 0.12f) to Color(0xFFEA580C)
        else -> FuelBluePrimary.copy(alpha = 0.12f) to FuelBluePrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = fuelType,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
