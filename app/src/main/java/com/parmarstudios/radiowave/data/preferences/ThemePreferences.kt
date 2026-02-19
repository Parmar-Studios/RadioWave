package com.parmarstudios.radiowave.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.parmarstudios.radiowave.ui.settings.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to provide a DataStore instance scoped to the application context
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Singleton object for managing theme preferences using DataStore.
 * Provides methods to observe and update the selected theme option.
 */
object ThemePreferences {
    // Key used to store the selected theme option in DataStore
    private val THEME_KEY = stringPreferencesKey("theme_option")

    /**
     * Returns a Flow that emits the current [com.parmarstudios.radiowave.ui.settings.ThemeOption] from DataStore.
     * Defaults to [com.parmarstudios.radiowave.ui.settings.ThemeOption.SYSTEM] if no value is set.
     *
     * @param context The application context.
     * @return A [kotlinx.coroutines.flow.Flow] emitting the current [com.parmarstudios.radiowave.ui.settings.ThemeOption].
     */
    fun themeFlow(context: Context): Flow<ThemeOption> =
        context.dataStore.data.map { prefs ->
            // Retrieve the stored theme option or default to SYSTEM
            ThemeOption.values().find { it.name == prefs[THEME_KEY] } ?: ThemeOption.SYSTEM
        }

    /**
     * Persists the selected [ThemeOption] to DataStore.
     *
     * @param context The application context.
     * @param option The [ThemeOption] to save.
     */
    suspend fun setTheme(context: Context, option: ThemeOption) {
        context.dataStore.edit { prefs ->
            // Store the theme option as a string
            prefs[THEME_KEY] = option.name
        }
    }
}