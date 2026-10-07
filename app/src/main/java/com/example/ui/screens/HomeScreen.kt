package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppItem
import com.example.data.AppsManager
import com.example.data.UserPreferences
import com.example.data.WallpaperType
import com.example.ui.components.AppIconItem
import com.example.ui.components.DockBar
import com.example.ui.components.QuickTogglesWidget
import com.example.ui.components.SearchBarWidget
import com.example.ui.components.SmartClockWidget

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    homeApps: List<AppItem>,
    dockApps: List<AppItem>,
    batteryPercent: Int,
    isCharging: Boolean,
    isTorchOn: Boolean,
    deviceInfo: AppsManager.DeviceInfo,
    searchQuery: String,
    mathResult: String?,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onToggleTorch: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("home_screen_root")
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Swipe up opens drawer
                    if (dragAmount < -30) {
                        onOpenDrawer()
                    }
                }
            }
    ) {
        // Wallpaper background
        LauncherWallpaperBackground(wallpaperType = preferences.wallpaper)

        // Semi-transparent overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22000000),
                            Color(0x55000000),
                            Color(0x88000000)
                        )
                    )
                )
        )

        // Main Home Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لانشر بسيط",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Clock & Weather Widget
                SmartClockWidget(
                    useArabicNumerals = preferences.useArabicNumerals,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Toggles (Torch, Battery, Storage, Note, Wi-Fi)
                QuickTogglesWidget(
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    isTorchOn = isTorchOn,
                    onToggleTorch = onToggleTorch,
                    onOpenNote = onOpenNote,
                    deviceInfo = deviceInfo,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                SearchBarWidget(
                    query = searchQuery,
                    onQueryChanged = onSearchQueryChanged,
                    mathResult = mathResult,
                    onSearchSubmit = onSearchSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sticky Note Preview Card if user has a saved note
                if (preferences.quickNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x30FFB74D))
                            .border(1.dp, Color(0x66FFB74D), RoundedCornerShape(16.dp))
                            .clickable { onOpenNote() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = preferences.quickNote,
                            color = Color(0xFFFFECB3),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0x99FFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pinned Apps Section Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "التطبيقات المثبتة",
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اضغط مطولاً للخيارات",
                        color = Color(0x88FFFFFF),
                        fontSize = 10.sp
                    )
                }

                // Pinned Apps Grid
                val columns = preferences.gridColumns
                val chunked = homeApps.chunked(columns)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                ) {
                    chunked.forEach { rowApps ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowApps.forEach { app ->
                                AppIconItem(
                                    app = app,
                                    iconStyle = preferences.iconStyle,
                                    iconSize = 54.dp,
                                    showLabel = true,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining columns if any
                            if (rowApps.size < columns) {
                                repeat(columns - rowApps.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Swipe up affordance
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDrawer() }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up for apps",
                        tint = Color(0x88FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "اسحب للأعلى لفتح جميع التطبيقات",
                        color = Color(0x88FFFFFF),
                        fontSize = 11.sp
                    )
                }
            }

            // Bottom Dock
            DockBar(
                dockApps = dockApps,
                iconStyle = preferences.iconStyle,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}

@Composable
fun LauncherWallpaperBackground(wallpaperType: WallpaperType) {
    when (wallpaperType) {
        WallpaperType.COSMIC -> {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_cosmic),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        WallpaperType.DUNES -> {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_dunes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        WallpaperType.AMOLED_BLACK -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
        WallpaperType.GRADIENT_CYBER -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A0033),
                                Color(0xFF311B92),
                                Color(0xFF006064)
                            )
                        )
                    )
            )
        }
        WallpaperType.GRADIENT_EMERALD -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF00251A),
                                Color(0xFF004D40),
                                Color(0xFF00796B)
                            )
                        )
                    )
            )
        }
    }
}
