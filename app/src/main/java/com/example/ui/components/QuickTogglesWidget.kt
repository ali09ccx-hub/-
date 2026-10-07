package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppsManager

@Composable
fun QuickTogglesWidget(
    batteryPercent: Int,
    isCharging: Boolean,
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onOpenNote: () -> Unit,
    deviceInfo: AppsManager.DeviceInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val torchBgColor by animateColorAsState(
        targetValue = if (isTorchOn) Color(0xFFFFD600) else Color(0x22FFFFFF),
        label = "torchBg"
    )
    val torchIconColor by animateColorAsState(
        targetValue = if (isTorchOn) Color.Black else Color.White,
        label = "torchIcon"
    )

    Row(
        modifier = modifier
            .testTag("quick_toggles_widget")
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery capsule
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = if (batteryPercent < 20) Color(0xFFFF5252) else Color(0xFF00E676),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$batteryPercent%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Storage / RAM pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable {
                    AppsManager.openSystemSetting(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                }
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Storage",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${deviceInfo.freeStorageGb}G متاح",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Quick Note button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable { onOpenNote() }
        ) {
            Icon(
                imageVector = Icons.Default.NoteAlt,
                contentDescription = "Quick Note",
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(20.dp)
            )
        }

        // Flashlight button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(torchBgColor)
                .border(1.dp, if (isTorchOn) Color(0xFFFFD600) else Color(0x33FFFFFF), CircleShape)
                .clickable { onToggleTorch() }
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Torch",
                tint = torchIconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Wi-Fi Shortcut
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable {
                    AppsManager.openSystemSetting(context, Settings.ACTION_WIFI_SETTINGS)
                }
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = Color(0xFF81D4FA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
