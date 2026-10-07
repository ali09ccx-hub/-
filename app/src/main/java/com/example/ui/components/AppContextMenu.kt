package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppItem
import com.example.data.AppsManager
import com.example.data.IconStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppItem,
    isFavorite: Boolean,
    isDocked: Boolean,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDock: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E222D),
        contentColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .testTag("app_context_menu")
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // App Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconItem(
                    app = app,
                    iconStyle = IconStyle.ROUNDED_SQUIRCLE,
                    iconSize = 48.dp,
                    showLabel = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.packageName,
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(10.dp))

            // Actions list
            MenuActionItem(
                icon = Icons.Default.Launch,
                title = "فتح التطبيق",
                tint = Color(0xFF00E5FF),
                onClick = {
                    onDismiss()
                    onLaunch()
                }
            )

            MenuActionItem(
                icon = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                title = if (isFavorite) "إزالة من المفضلة (الرئيسية)" else "تثبيت في المفضلة (الرئيسية)",
                tint = Color(0xFFFFB300),
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            MenuActionItem(
                icon = Icons.Default.ViewSidebar,
                title = if (isDocked) "إزالة من الشريط السفلي" else "تثبيت في الشريط السفلي",
                tint = Color(0xFF69F0AE),
                onClick = {
                    onToggleDock()
                    onDismiss()
                }
            )

            MenuActionItem(
                icon = Icons.Default.Info,
                title = "معلومات التطبيق",
                tint = Color(0xFF40C4FF),
                onClick = {
                    onDismiss()
                    AppsManager.openAppInfo(context, app.packageName)
                }
            )

            MenuActionItem(
                icon = Icons.Default.Delete,
                title = "إلغاء التثبيت",
                tint = Color(0xFFFF5252),
                onClick = {
                    onDismiss()
                    AppsManager.requestUninstall(context, app.packageName)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MenuActionItem(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
