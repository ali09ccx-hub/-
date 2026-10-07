package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppsManager
import com.example.ui.LauncherViewModel
import com.example.ui.components.AppContextMenu
import com.example.ui.components.QuickNoteDialog
import com.example.ui.components.SettingsSheet
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MinimalHomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                        ) {
                            LauncherApp(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadApps()
    }
}

@Composable
fun LauncherApp(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    // Handle Back Press on Home Screen (stay on home or close open sheets)
    BackHandler(enabled = uiState.isDrawerOpen || uiState.isSettingsOpen || uiState.isNoteDialogOpen || uiState.selectedAppForMenu != null) {
        when {
            uiState.selectedAppForMenu != null -> viewModel.setSelectedAppForMenu(null)
            uiState.isNoteDialogOpen -> viewModel.setNoteDialogOpen(false)
            uiState.isSettingsOpen -> viewModel.setSettingsOpen(false)
            uiState.isDrawerOpen -> viewModel.setDrawerOpen(false)
        }
    }

    val onSearchSubmitAction: (String) -> Unit = { query ->
        val trimmed = query.trim()
        val matchingApp = uiState.allApps.find {
            it.label.equals(trimmed, ignoreCase = true)
        }
        if (matchingApp != null) {
            AppsManager.launchApp(context, matchingApp.packageName, matchingApp.activityName)
        } else {
            AppsManager.openSearch(context, trimmed)
        }
    }

    if (preferences.minimalistMode) {
        MinimalHomeScreen(
            favoriteApps = uiState.homeFavorites,
            useArabicNumerals = preferences.useArabicNumerals,
            onAppClick = { app ->
                AppsManager.launchApp(context, app.packageName, app.activityName)
            },
            onAppLongClick = { app ->
                viewModel.setSelectedAppForMenu(app)
            },
            onOpenDrawer = { viewModel.setDrawerOpen(true) },
            onOpenSettings = { viewModel.setSettingsOpen(true) },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        HomeScreen(
            preferences = preferences,
            homeApps = uiState.homeFavorites,
            dockApps = uiState.dockApps,
            batteryPercent = uiState.batteryPercent,
            isCharging = uiState.isCharging,
            isTorchOn = uiState.isTorchOn,
            deviceInfo = uiState.deviceInfo,
            searchQuery = uiState.searchQuery,
            mathResult = uiState.mathEvaluation,
            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onSearchSubmit = onSearchSubmitAction,
            onToggleTorch = { viewModel.toggleTorch(context) },
            onOpenNote = { viewModel.setNoteDialogOpen(true) },
            onOpenSettings = { viewModel.setSettingsOpen(true) },
            onOpenDrawer = { viewModel.setDrawerOpen(true) },
            onAppClick = { app ->
                AppsManager.launchApp(context, app.packageName, app.activityName)
            },
            onAppLongClick = { app ->
                viewModel.setSelectedAppForMenu(app)
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // App Drawer Layer (Full screen overlay with animation)
    AnimatedVisibility(
        visible = uiState.isDrawerOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        AppDrawerScreen(
            apps = uiState.filteredApps,
            selectedCategory = uiState.selectedCategory,
            searchQuery = uiState.searchQuery,
            mathResult = uiState.mathEvaluation,
            preferences = preferences,
            onCategorySelected = { viewModel.selectCategory(it) },
            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onSearchSubmit = onSearchSubmitAction,
            onAppClick = { app ->
                AppsManager.launchApp(context, app.packageName, app.activityName)
            },
            onAppLongClick = { app ->
                viewModel.setSelectedAppForMenu(app)
            },
            onCloseDrawer = { viewModel.setDrawerOpen(false) }
        )
    }

    // Settings / Customizer Sheet
    if (uiState.isSettingsOpen) {
        SettingsSheet(
            preferences = preferences,
            onSelectWallpaper = { viewModel.setWallpaper(it) },
            onSelectTheme = { viewModel.setThemeMode(it) },
            onSelectIconStyle = { viewModel.setIconStyle(it) },
            onToggleMinimalist = { viewModel.toggleMinimalistMode() },
            onSelectGridColumns = { viewModel.setGridColumns(it) },
            onToggleArabicNumerals = { viewModel.toggleArabicNumerals() },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }

    // Quick Note Dialog
    if (uiState.isNoteDialogOpen) {
        QuickNoteDialog(
            initialNote = preferences.quickNote,
            onSave = { viewModel.saveQuickNote(it) },
            onDismiss = { viewModel.setNoteDialogOpen(false) }
        )
    }

    // App Context Menu Sheet
    uiState.selectedAppForMenu?.let { app ->
        AppContextMenu(
            app = app,
            isFavorite = preferences.favoritePackages.contains(app.packageName),
            isDocked = preferences.dockPackages.contains(app.packageName),
            onDismiss = { viewModel.setSelectedAppForMenu(null) },
            onLaunch = {
                AppsManager.launchApp(context, app.packageName, app.activityName)
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(app)
            },
            onToggleDock = {
                viewModel.toggleDock(app)
            }
        )
    }
}
