package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "simple_launcher_prefs")

data class UserPreferences(
    val wallpaper: WallpaperType = WallpaperType.COSMIC,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val iconStyle: IconStyle = IconStyle.GLASS_NEON,
    val minimalistMode: Boolean = false,
    val gridColumns: Int = 4,
    val quickNote: String = "",
    val favoritePackages: Set<String> = emptySet(),
    val dockPackages: Set<String> = emptySet(),
    val useArabicNumerals: Boolean = true
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val WALLPAPER = stringPreferencesKey("pref_wallpaper")
        val THEME_MODE = stringPreferencesKey("pref_theme_mode")
        val ICON_STYLE = stringPreferencesKey("pref_icon_style")
        val MINIMALIST_MODE = booleanPreferencesKey("pref_minimalist_mode")
        val GRID_COLUMNS = intPreferencesKey("pref_grid_columns")
        val QUICK_NOTE = stringPreferencesKey("pref_quick_note")
        val FAVORITE_PACKAGES = stringSetPreferencesKey("pref_favorite_packages")
        val DOCK_PACKAGES = stringSetPreferencesKey("pref_dock_packages")
        val USE_ARABIC_NUMERALS = booleanPreferencesKey("pref_arabic_numerals")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val wallpaperName = preferences[Keys.WALLPAPER] ?: WallpaperType.COSMIC.name
        val themeModeName = preferences[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val iconStyleName = preferences[Keys.ICON_STYLE] ?: IconStyle.GLASS_NEON.name
        val isMinimalist = preferences[Keys.MINIMALIST_MODE] ?: false
        val gridColumns = preferences[Keys.GRID_COLUMNS] ?: 4
        val quickNote = preferences[Keys.QUICK_NOTE] ?: ""
        val favorites = preferences[Keys.FAVORITE_PACKAGES] ?: emptySet()
        val dock = preferences[Keys.DOCK_PACKAGES] ?: emptySet()
        val arabicNums = preferences[Keys.USE_ARABIC_NUMERALS] ?: true

        UserPreferences(
            wallpaper = runCatching { WallpaperType.valueOf(wallpaperName) }.getOrDefault(WallpaperType.COSMIC),
            themeMode = runCatching { ThemeMode.valueOf(themeModeName) }.getOrDefault(ThemeMode.DARK),
            iconStyle = runCatching { IconStyle.valueOf(iconStyleName) }.getOrDefault(IconStyle.GLASS_NEON),
            minimalistMode = isMinimalist,
            gridColumns = gridColumns.coerceIn(3, 5),
            quickNote = quickNote,
            favoritePackages = favorites,
            dockPackages = dock,
            useArabicNumerals = arabicNums
        )
    }

    suspend fun setWallpaper(wallpaper: WallpaperType) {
        context.dataStore.edit { it[Keys.WALLPAPER] = wallpaper.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setIconStyle(style: IconStyle) {
        context.dataStore.edit { it[Keys.ICON_STYLE] = style.name }
    }

    suspend fun setMinimalistMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MINIMALIST_MODE] = enabled }
    }

    suspend fun setGridColumns(cols: Int) {
        context.dataStore.edit { it[Keys.GRID_COLUMNS] = cols }
    }

    suspend fun setQuickNote(note: String) {
        context.dataStore.edit { it[Keys.QUICK_NOTE] = note }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.FAVORITE_PACKAGES] = current
        }
    }

    suspend fun toggleDock(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.DOCK_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                if (current.size < 5) {
                    current.add(packageName)
                }
            }
            prefs[Keys.DOCK_PACKAGES] = current
        }
    }

    suspend fun setArabicNumerals(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_ARABIC_NUMERALS] = enabled }
    }
}
