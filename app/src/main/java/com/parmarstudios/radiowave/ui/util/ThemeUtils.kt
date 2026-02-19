// File: ThemeUtils.kt
package com.parmarstudios.radiowave.ui.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.parmarstudios.radiowave.ui.settings.ThemeOption

@Composable
fun isAppInDarkTheme(themeOption: ThemeOption): Boolean {
    return when (themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }
}