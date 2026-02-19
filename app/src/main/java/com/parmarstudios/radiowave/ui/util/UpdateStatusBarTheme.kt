package com.parmarstudios.radiowave.ui.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.example.compose.primaryContainerLight
import com.example.compose.surfaceDimDarkMediumContrast
import com.parmarstudios.radiowave.ui.settings.ThemeOption

@Composable
fun UpdateStatusBarTheme(themeOption: ThemeOption) {
    val view = LocalView.current
    val isDarkTheme = isAppInDarkTheme(themeOption) // Call composable here
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color based on theme
            window.statusBarColor = if (isDarkTheme) surfaceDimDarkMediumContrast.toArgb() else primaryContainerLight.toArgb()
            // Set status bar icon appearance
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme
        }
    }
}