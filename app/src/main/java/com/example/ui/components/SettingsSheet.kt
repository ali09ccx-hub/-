package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppsManager
import com.example.data.IconStyle
import com.example.data.ThemeMode
import com.example.data.UserPreferences
import com.example.data.WallpaperType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: UserPreferences,
    onSelectWallpaper: (WallpaperType) -> Unit,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectIconStyle: (IconStyle) -> Unit,
    onToggleMinimalist: () -> Unit,
    onSelectGridColumns: (Int) -> Unit,
    onToggleArabicNumerals: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131722),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .testTag("launcher_settings_sheet")
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "تخصيص اللانشر",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Wallpaper Picker
            SectionHeader(icon = Icons.Default.Image, title = "خلفية الشاشة")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                items(WallpaperType.values()) { wp ->
                    val isSelected = preferences.wallpaper == wp
                    val gradient = when (wp) {
                        WallpaperType.COSMIC -> listOf(Color(0xFF0D1B2A), Color(0xFF4A148C), Color(0xFF00E5FF))
                        WallpaperType.DUNES -> listOf(Color(0xFFFF8A65), Color(0xFFFFB74D), Color(0xFF3E2723))
                        WallpaperType.AMOLED_BLACK -> listOf(Color.Black, Color(0xFF111111))
                        WallpaperType.GRADIENT_CYBER -> listOf(Color(0xFF6200EA), Color(0xFF00B0FF))
                        WallpaperType.GRADIENT_EMERALD -> listOf(Color(0xFF004D40), Color(0xFF00BFA5))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(Color(0x22FFFFFF))
                            .clickable { onSelectWallpaper(wp) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 74.dp, height = 74.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(gradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = wp.titleAr,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1AFFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // 2. Icon Style
            SectionHeader(icon = Icons.Default.Palette, title = "نمط وتصميم الأيقونات")
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                IconStyle.values().forEach { style ->
                    val isSelected = preferences.iconStyle == style
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else Color(0x15FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color(0x22FFFFFF),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectIconStyle(style) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = style.titleAr,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = style.descriptionAr,
                                color = Color(0x99FFFFFF),
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1AFFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // 3. Grid Columns
            SectionHeader(icon = Icons.Default.GridOn, title = "عدد أعمدة التطبيقات")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                listOf(3, 4, 5).forEach { cols ->
                    val isSelected = preferences.gridColumns == cols
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF00E5FF) else Color(0x18FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color(0x22FFFFFF),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectGridColumns(cols) }
                    ) {
                        Text(
                            text = "$cols أعمدة",
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1AFFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // 4. Minimalist Detox Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x15FFFFFF))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = Color(0xFFB388FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "وضع البساطة النصي (Minimalist Detox)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عرض التطبيقات كنصوص أنيقة بدون تشتت أيقونات",
                            color = Color(0x99FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = preferences.minimalistMode,
                    onCheckedChange = { onToggleMinimalist() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFB388FF),
                        checkedTrackColor = Color(0x66B388FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Arabic Numerals Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x15FFFFFF))
                    .padding(14.dp)
            ) {
                Text(
                    text = "استخدام الأرقام العربية (١ ٢ ٣)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = preferences.useArabicNumerals,
                    onCheckedChange = { onToggleArabicNumerals() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0x6600E5FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Set as Default Launcher Button
            Button(
                onClick = {
                    AppsManager.openHomeSettings(context)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تعيين كلانشر رئيسي للجهاز",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
