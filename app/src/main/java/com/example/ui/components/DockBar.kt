package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppItem
import com.example.data.IconStyle

@Composable
fun DockBar(
    dockApps: List<AppItem>,
    iconStyle: IconStyle,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("launcher_dock_bar")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x6600E5FF))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x35FFFFFF),
                            Color(0x18000000)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x44FFFFFF),
                            Color(0x1100E5FF)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render up to 4 dock apps
            val displayApps = dockApps.take(4)
            displayApps.forEach { app ->
                AppIconItem(
                    app = app,
                    iconStyle = iconStyle,
                    iconSize = 48.dp,
                    showLabel = false,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            // Central / End App Drawer button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF7C4DFF)
                            )
                        )
                    )
                    .clickable { onOpenDrawer() }
                    .testTag("open_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "All Apps",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
