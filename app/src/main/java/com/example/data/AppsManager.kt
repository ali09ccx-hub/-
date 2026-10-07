package com.example.data

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import java.io.File
import java.util.Locale

object AppsManager {

    fun getInstalledApps(context: Context): List<AppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val selfPackage = context.packageName

        val realApps = resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val actName = resolveInfo.activityInfo.name
            if (pkg == selfPackage) return@mapNotNull null

            val label = runCatching { resolveInfo.loadLabel(pm).toString() }.getOrDefault(pkg)
            val icon = runCatching { resolveInfo.loadIcon(pm) }.getOrNull()
            val category = detectCategory(resolveInfo.activityInfo.applicationInfo, pkg, label)

            AppItem(
                id = "$pkg/$actName",
                packageName = pkg,
                activityName = actName,
                label = label,
                icon = icon,
                category = category,
                installTime = runCatching {
                    pm.getPackageInfo(pkg, 0).firstInstallTime
                }.getOrDefault(0L)
            )
        }

        // Complement with standard system apps if running in emulator with sparse launcher apps
        return if (realApps.size < 4) {
            val defaults = getFallbackDefaultApps(context)
            (realApps + defaults).distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.ROOT) }
        } else {
            realApps.sortedBy { it.label.lowercase(Locale.ROOT) }
        }
    }

    private fun detectCategory(appInfo: ApplicationInfo, packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase(Locale.ROOT)
        val lowerLabel = label.lowercase(Locale.ROOT)

        if (lowerPkg.contains("social") || lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
            lowerPkg.contains("instagram") || lowerPkg.contains("facebook") || lowerPkg.contains("twitter") ||
            lowerPkg.contains("message") || lowerPkg.contains("dialer") || lowerPkg.contains("contacts") ||
            lowerLabel.contains("اتصال") || lowerLabel.contains("هاتف") || lowerLabel.contains("رسائل") ||
            lowerLabel.contains("جهات") || lowerLabel.contains("واتساب")
        ) {
            return AppCategory.SOCIAL
        }

        if (lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") ||
            lowerPkg.contains("youtube") || lowerPkg.contains("music") || lowerPkg.contains("player") ||
            lowerPkg.contains("video") || lowerLabel.contains("كاميرا") || lowerLabel.contains("معرض") ||
            lowerLabel.contains("صور") || lowerLabel.contains("يوتيوب")
        ) {
            return AppCategory.MEDIA
        }

        if (lowerPkg.contains("settings") || lowerPkg.contains("system") || lowerPkg.contains("android") ||
            lowerLabel.contains("إعدادات") || lowerLabel.contains("ضبط")
        ) {
            return AppCategory.SYSTEM
        }

        if (lowerPkg.contains("calc") || lowerPkg.contains("clock") || lowerPkg.contains("calendar") ||
            lowerPkg.contains("note") || lowerPkg.contains("tool") || lowerPkg.contains("browser") ||
            lowerPkg.contains("chrome") || lowerPkg.contains("file") || lowerLabel.contains("حاسبة") ||
            lowerLabel.contains("ساعة") || lowerLabel.contains("تقويم") || lowerLabel.contains("ملفات") ||
            lowerLabel.contains("متصفح") || lowerLabel.contains("ملاحظات")
        ) {
            return AppCategory.TOOLS
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_IMAGE, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_AUDIO -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_MAPS, ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.TOOLS
            }
        }

        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        return if (isSystem) AppCategory.SYSTEM else AppCategory.TOOLS
    }

    private fun getFallbackDefaultApps(context: Context): List<AppItem> {
        val pm = context.packageManager
        val fallbacks = listOf(
            Triple("الهاتف", "com.google.android.dialer", AppCategory.SOCIAL),
            Triple("الرسائل", "com.google.android.apps.messaging", AppCategory.SOCIAL),
            Triple("المتصفح", "com.android.chrome", AppCategory.TOOLS),
            Triple("الكاميرا", "com.android.camera2", AppCategory.MEDIA),
            Triple("الصور", "com.google.android.apps.photos", AppCategory.MEDIA),
            Triple("الإعدادات", "com.android.settings", AppCategory.SYSTEM),
            Triple("الساعة", "com.google.android.deskclock", AppCategory.TOOLS),
            Triple("الحاسبة", "com.google.android.calculator", AppCategory.TOOLS),
            Triple("الملفات", "com.google.android.documentsui", AppCategory.TOOLS),
            Triple("الخرائط", "com.google.android.apps.maps", AppCategory.TOOLS),
            Triple("الملاحظات", "com.google.android.keep", AppCategory.TOOLS),
            Triple("متجر التطبيقات", "com.android.vending", AppCategory.SYSTEM)
        )

        return fallbacks.map { (title, pkg, cat) ->
            val icon = runCatching { pm.getApplicationIcon(pkg) }.getOrNull()
            AppItem(
                id = "$pkg/main",
                packageName = pkg,
                activityName = "",
                label = title,
                icon = icon,
                category = cat,
                installTime = System.currentTimeMillis()
            )
        }
    }

    fun launchApp(context: Context, packageName: String, activityName: String) {
        try {
            val pm = context.packageManager
            var launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent == null && activityName.isNotEmpty()) {
                launchIntent = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(packageName, activityName)
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
            }
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "تعذر فتح: $packageName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء محاولة فتح التطبيق", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح معلومات التطبيق", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestUninstall(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر طلب إلغاء التثبيت", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSearch(context: Context, query: String) {
        try {
            val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح البحث", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleTorch(context: Context, currentState: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                val newState = !currentState
                cameraManager.setTorchMode(cameraId, newState)
                newState
            } else {
                !currentState
            }
        } catch (e: CameraAccessException) {
            !currentState
        } catch (e: Exception) {
            !currentState
        }
    }

    fun openSystemSetting(context: Context, settingAction: String) {
        try {
            val intent = Intent(settingAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {
                Toast.makeText(context, "تعذر فتح الإعدادات", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openHomeSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openSystemSetting(context, Settings.ACTION_SETTINGS)
        }
    }

    data class DeviceInfo(
        val freeStorageGb: Double,
        val totalStorageGb: Double,
        val freeRamMb: Long,
        val totalRamMb: Long
    )

    fun getDeviceInfo(context: Context): DeviceInfo {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            val rawTotal = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val rawFree = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val totalStorage = if (rawTotal > 0.0) rawTotal else 64.0
            val freeStorage = if (rawFree > 0.0) rawFree else 24.5

            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val totalRam = if (memInfo.totalMem > 0) memInfo.totalMem / (1024 * 1024) else 4096
            val freeRam = if (memInfo.availMem > 0) memInfo.availMem / (1024 * 1024) else 1850

            DeviceInfo(
                freeStorageGb = String.format(Locale.US, "%.1f", freeStorage).toDoubleOrNull() ?: 16.0,
                totalStorageGb = String.format(Locale.US, "%.1f", totalStorage).toDoubleOrNull() ?: 64.0,
                freeRamMb = freeRam,
                totalRamMb = totalRam
            )
        } catch (e: Exception) {
            DeviceInfo(24.5, 64.0, 1850, 4096)
        }
    }
}
