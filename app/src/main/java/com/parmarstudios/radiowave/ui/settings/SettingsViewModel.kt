package com.parmarstudios.radiowave.ui.settings

import android.app.Application
import android.text.format.Formatter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.parmarstudios.radiowave.data.preferences.ThemePreferences
import com.parmarstudios.radiowave.data.db.AppDatabase
import com.parmarstudios.radiowave.data.db.BlockedStationDao
import com.parmarstudios.radiowave.data.db.FavoriteStationDao
import com.parmarstudios.radiowave.data.db.RecentlyPlayedStationDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    val theme: StateFlow<ThemeOption> = ThemePreferences.themeFlow(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeOption.SYSTEM)


    // Initialize database and DAOs here
    private val db: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    private val historyDao: RecentlyPlayedStationDao by lazy { db.recentlyPlayedStationDao() }
    private val favoriteDao: FavoriteStationDao by lazy { db.favoriteStationDao() }
    private val blockedDao: BlockedStationDao by lazy { db.blockedStationDao() }

    private val _historySize = MutableStateFlow("Calculating...")
    val historySize: StateFlow<String> = _historySize.asStateFlow()
    private val _favoritesSize = MutableStateFlow("Calculating...")
    val favoritesSize: StateFlow<String> = _favoritesSize.asStateFlow()
    private val _blockedSize = MutableStateFlow("Calculating...")
    val blockedSize: StateFlow<String> = _blockedSize.asStateFlow()

    init {
        refreshSizes()
    }

    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            ThemePreferences.setTheme(context, option)
        }
    }

    fun refreshSizes() {
        viewModelScope.launch {
            val historyBytes = getHistoryBytes()
            val favoritesBytes = getFavoritesBytes()
            val blockedBytes = getBlockedBytes()
            _historySize.value = formatSize(historyBytes)
            _favoritesSize.value = formatSize(favoritesBytes)
            _blockedSize.value = formatSize(blockedBytes)
        }
    }

    private fun formatSize(bytes: Long): String =
        Formatter.formatShortFileSize(context, bytes)

    private suspend fun getHistoryBytes(): Long {
        val entries = historyDao.getAllRecentlyPlayedStations()
        android.util.Log.d("SettingsViewModel", "History entries: ${entries.size}")
        return entries.sumOf { it.toString().toByteArray().size.toLong() }
    }

    private suspend fun getFavoritesBytes(): Long {
        val entries = favoriteDao.getAllFavorites()
        return entries.sumOf { it.toString().toByteArray().size.toLong() }
    }

    private suspend fun getBlockedBytes(): Long {
        val entries = blockedDao.getAllBlocked()
        return entries.sumOf { it.toString().toByteArray().size.toLong() }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
            refreshSizes()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            // Add a clearAll() method to FavoriteStationDao if not present
            favoriteDao.clearAll()
            refreshSizes()
        }
    }

    fun clearBlocked() {
        viewModelScope.launch {
            // Add a clearAll() method to BlockedStationDao if not present
            blockedDao.clearAll()
            refreshSizes()
        }
    }
}