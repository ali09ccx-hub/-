package com.example.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppCategory
import com.example.data.AppItem
import com.example.data.AppsManager
import com.example.data.IconStyle
import com.example.data.LauncherPreferencesRepository
import com.example.data.ThemeMode
import com.example.data.UserPreferences
import com.example.data.WallpaperType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class LauncherUiState(
    val allApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val homeFavorites: List<AppItem> = emptyList(),
    val dockApps: List<AppItem> = emptyList(),
    val selectedCategory: AppCategory = AppCategory.ALL,
    val searchQuery: String = "",
    val mathEvaluation: String? = null,
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isTorchOn: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isNoteDialogOpen: Boolean = false,
    val selectedAppForMenu: AppItem? = null,
    val deviceInfo: AppsManager.DeviceInfo = AppsManager.DeviceInfo(24.5, 64.0, 1850, 4096)
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = LauncherPreferencesRepository(application)

    val preferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
                _uiState.update { it.copy(batteryPercent = pct, isCharging = isCharging) }
            }
        }
    }

    init {
        loadApps()
        val appContext = getApplication<Application>()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        appContext.registerReceiver(batteryReceiver, filter)

        viewModelScope.launch {
            _uiState.update { it.copy(deviceInfo = AppsManager.getDeviceInfo(appContext)) }
        }

        // Recompute apps when preferences change
        viewModelScope.launch {
            preferences.collect { userPrefs ->
                recomputeApps(userPrefs)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (ignored: Exception) {
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = AppsManager.getInstalledApps(getApplication())
            val userPrefs = preferences.value
            _uiState.update { state ->
                val (favorites, dock) = computeHomeAndDock(apps, userPrefs)
                state.copy(
                    allApps = apps,
                    filteredApps = filterAppsList(apps, state.selectedCategory, state.searchQuery),
                    homeFavorites = favorites,
                    dockApps = dock
                )
            }
        }
    }

    private fun recomputeApps(userPrefs: UserPreferences) {
        _uiState.update { state ->
            val (favorites, dock) = computeHomeAndDock(state.allApps, userPrefs)
            state.copy(
                filteredApps = filterAppsList(state.allApps, state.selectedCategory, state.searchQuery),
                homeFavorites = favorites,
                dockApps = dock
            )
        }
    }

    private fun computeHomeAndDock(
        apps: List<AppItem>,
        userPrefs: UserPreferences
    ): Pair<List<AppItem>, List<AppItem>> {
        val favPackages = userPrefs.favoritePackages
        val dockPackages = userPrefs.dockPackages

        val favorites = if (favPackages.isNotEmpty()) {
            apps.filter { it.packageName in favPackages }
        } else {
            // Default 8 popular apps if none pinned yet
            apps.take(8)
        }

        val dock = if (dockPackages.isNotEmpty()) {
            apps.filter { it.packageName in dockPackages }.take(5)
        } else {
            // Default essential dock apps (Phone, Messages, Browser, Camera)
            val dockDefaults = listOf("dialer", "message", "chrome", "browser", "camera")
            val chosen = mutableListOf<AppItem>()
            for (kw in dockDefaults) {
                apps.find { it.packageName.lowercase(Locale.ROOT).contains(kw) }?.let {
                    if (!chosen.contains(it)) chosen.add(it)
                }
            }
            if (chosen.size < 4) {
                chosen.addAll(apps.take(4 - chosen.size))
            }
            chosen.distinctBy { it.packageName }.take(4)
        }

        return Pair(favorites, dock)
    }

    private fun filterAppsList(
        apps: List<AppItem>,
        category: AppCategory,
        query: String
    ): List<AppItem> {
        val trimmedQuery = query.trim().lowercase(Locale.ROOT)
        return apps.filter { app ->
            val matchesCategory = when (category) {
                AppCategory.ALL -> true
                AppCategory.FAVORITES -> preferences.value.favoritePackages.contains(app.packageName)
                else -> app.category == category
            }
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                app.label.lowercase(Locale.ROOT).contains(trimmedQuery) ||
                        app.packageName.lowercase(Locale.ROOT).contains(trimmedQuery)
            }
            matchesCategory && matchesQuery
        }
    }

    fun onSearchQueryChanged(query: String) {
        val mathResult = evaluateMath(query)
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                mathEvaluation = mathResult,
                filteredApps = filterAppsList(state.allApps, state.selectedCategory, query)
            )
        }
    }

    private fun evaluateMath(expr: String): String? {
        val clean = expr.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        val mathRegex = Regex("""^(\d+(\.\d+)?)([\+\-\*\/])(\d+(\.\d+)?)$""")
        val match = mathRegex.matchEntire(clean) ?: return null
        val (leftStr, _, op, rightStr) = match.destructured

        val left = leftStr.toDoubleOrNull() ?: return null
        val right = rightStr.toDoubleOrNull() ?: return null

        val res = when (op) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> if (right != 0.0) left / right else null
            else -> null
        } ?: return null

        return if (res % 1.0 == 0.0) {
            res.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", res)
        }
    }

    fun selectCategory(category: AppCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredApps = filterAppsList(state.allApps, category, state.searchQuery)
            )
        }
    }

    fun setDrawerOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isDrawerOpen = isOpen) }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setNoteDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isNoteDialogOpen = isOpen) }
    }

    fun setSelectedAppForMenu(app: AppItem?) {
        _uiState.update { it.copy(selectedAppForMenu = app) }
    }

    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            prefsRepo.toggleFavorite(app.packageName)
        }
    }

    fun toggleDock(app: AppItem) {
        viewModelScope.launch {
            prefsRepo.toggleDock(app.packageName)
        }
    }

    fun setWallpaper(wallpaper: WallpaperType) {
        viewModelScope.launch {
            prefsRepo.setWallpaper(wallpaper)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun setIconStyle(style: IconStyle) {
        viewModelScope.launch {
            prefsRepo.setIconStyle(style)
        }
    }

    fun toggleMinimalistMode() {
        viewModelScope.launch {
            val current = preferences.value.minimalistMode
            prefsRepo.setMinimalistMode(!current)
        }
    }

    fun setGridColumns(cols: Int) {
        viewModelScope.launch {
            prefsRepo.setGridColumns(cols)
        }
    }

    fun saveQuickNote(note: String) {
        viewModelScope.launch {
            prefsRepo.setQuickNote(note)
        }
    }

    fun toggleArabicNumerals() {
        viewModelScope.launch {
            val current = preferences.value.useArabicNumerals
            prefsRepo.setArabicNumerals(!current)
        }
    }

    fun toggleTorch(context: Context) {
        val currentState = _uiState.value.isTorchOn
        val newState = AppsManager.toggleTorch(context, currentState)
        _uiState.update { it.copy(isTorchOn = newState) }
    }
}
