package com.parmarstudios.radiowave.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.parmarstudios.radiowave.data.RadioStation
import com.parmarstudios.radiowave.data.db.AppDatabase
import com.parmarstudios.radiowave.data.db.BlockedStation
import com.parmarstudios.radiowave.data.db.BlockedStationDao
import com.parmarstudios.radiowave.data.db.FavoriteStation
import com.parmarstudios.radiowave.data.db.FavoriteStationDao
import com.parmarstudios.radiowave.data.db.RecentlyPlayedStation
import com.parmarstudios.radiowave.data.db.RecentlyPlayedStationDao
import com.parmarstudios.radiowave.data.preferences.LastPlayingStation
import com.parmarstudios.radiowave.data.preferences.LastPlayingStationPreference
import com.parmarstudios.radiowave.network.RadioBrowserApi
import com.parmarstudios.radiowave.network.RadioBrowserHealthChecker
import com.parmarstudios.radiowave.network.StationSearchRequest
import com.parmarstudios.radiowave.player.PlaybackState
import com.parmarstudios.radiowave.player.RadioPlayer
import com.parmarstudios.radiowave.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class RadioStationsViewModel(context: Context) : ViewModel() {

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations: StateFlow<List<RadioStation>> = _stations

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchCountry = MutableStateFlow("")
    val searchCountry: StateFlow<String> = _searchCountry

    private val _playingStation = MutableStateFlow<RadioStation?>(null)
    val playingStation: StateFlow<RadioStation?> = _playingStation


    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _order = MutableStateFlow("name")
    val order: StateFlow<String> = _order

    private val _reverse = MutableStateFlow(false)
    val reverse: StateFlow<Boolean> = _reverse

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    private val _favoriteStations = MutableStateFlow<List<FavoriteStation>>(emptyList())
    val favoriteStations: StateFlow<List<FavoriteStation>> = _favoriteStations

    private val _blockedStations = MutableStateFlow<List<BlockedStation>>(emptyList())
    val blockedStations: StateFlow<List<BlockedStation>> = _blockedStations

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val _stationDetail = MutableStateFlow<RadioStation?>(null)
    val stationDetail: StateFlow<RadioStation?> = _stationDetail

    private val _stationDetailErrorMessage = MutableStateFlow<String?>(null)
    val stationDetailErrorMessage: StateFlow<String?> = _stationDetailErrorMessage

    var recentlyPlayedStations: StateFlow<List<RecentlyPlayedStation>> = MutableStateFlow(emptyList())

    private val _uiState = MutableStateFlow<StationListUiState>(StationListUiState.Loading)
    val uiState: StateFlow<StationListUiState> = _uiState

    private val db: AppDatabase = AppDatabase.getInstance(context.applicationContext)
    private val favoriteDao: FavoriteStationDao = db.favoriteStationDao()
    private val blockedDao: BlockedStationDao = db.blockedStationDao()
    private val recentlyPlayedStationDao: RecentlyPlayedStationDao = db.recentlyPlayedStationDao()


    private val healthChecker = RadioBrowserHealthChecker()
    private var api: RadioBrowserApi? = null

    // Pagination state
    private var currentPage = 0
    private val pageSize = 20
    private var isLastPage = false

    private var pendingStationToPlay: RadioStation? = null
    init {
        initApiWithHealthyServer()
    }

    fun initRecentlyPlayed() {
        recentlyPlayedStations = recentlyPlayedStationDao.getAllRecentlyPlayedStationsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }


    fun initFavorites() {

        viewModelScope.launch {
            _favoriteStations.value = favoriteDao.getAllFavorites()
        }
    }

    fun initBlocked() {

        viewModelScope.launch {
            _blockedStations.value = blockedDao.getAllBlocked()
        }
    }

    fun onSearchInputChange(query: String, country: String) {
        _searchQuery.value = query
        _searchCountry.value = country
        currentPage = 0
        isLastPage = false
        fetchStations(query, country)
    }

    private fun fetchStations(
        query: String = "",
        country: String = "",
        order: String = _order.value,
        reverse: Boolean = _reverse.value
    ) {
        viewModelScope.launch {
            _uiState.value = StationListUiState.Loading
            try {
                val request = StationSearchRequest(
                    country = country,
                    name = query,
                    order = order,
                    reverse = reverse,
                    limit = pageSize,
                    offset = 0
                )
                if (api != null) {
                    val result = api?.searchStations(request) ?: emptyList()
                    val distinctStations = result.distinctBy { it.urlResolved ?: it.url }
                    _stations.value = distinctStations
                    currentPage = 1
                    isLastPage = result.size < pageSize
                    _uiState.value = if (distinctStations.isEmpty()) {
                        StationListUiState.Empty
                    } else {
                        StationListUiState.Success(distinctStations)
                    }

                }
            } catch (e: Exception) {
                _stations.value = emptyList()
                _uiState.value = StationListUiState.Error("Failed to load stations")
            }
        }
    }

    fun loadMoreStations() {
        if (_isLoadingMore.value || isLastPage || _uiState.value is StationListUiState.Loading) return
        _isLoadingMore.value = true
        viewModelScope.launch {
            try {
                val request = StationSearchRequest(
                    country = _searchCountry.value,
                    name = _searchQuery.value,
                    limit = pageSize,
                    order = _order.value,
                    reverse = _reverse.value,
                    offset = currentPage * pageSize
                )

                val newStations = api?.searchStations(request) ?: emptyList()
                if (newStations.isEmpty() || newStations.size < pageSize) {
                    isLastPage = true
                }
                val updatedStations = (_stations.value + newStations).distinctBy { it.urlResolved ?: it.url }
                _stations.value = updatedStations
                currentPage++

                _uiState.value = if (updatedStations.isEmpty()) {
                    StationListUiState.Empty
                } else {
                    StationListUiState.Success(updatedStations)
                }
            } catch (e: Exception) {
                _uiState.value = StationListUiState.Error("Failed to load more stations")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }


    fun playStation(context: Context, station: RadioStation) {
        if (_playingStation.value != null) {
            // If a station is already playing, set pending and stop current
            pendingStationToPlay = station
            stopPlayback(context)
            return
        }

        _playingStation.value = station
        _playbackState.value = PlaybackState.LOADING
        saveLastPlayingStation(context, station, _playbackState.value, _elapsedSeconds.value)
        addRecentlyPlayedStation(station, System.currentTimeMillis())
        val url = station.urlResolved ?: station.url
        val intent = Intent(context, com.parmarstudios.radiowave.player.RadioPlayerService::class.java).apply {
            action = com.parmarstudios.radiowave.player.RadioPlayerService.ACTION_PLAY
            putExtra(com.parmarstudios.radiowave.player.RadioPlayerService.EXTRA_URL, url)
            putExtra(com.parmarstudios.radiowave.player.RadioPlayerService.EXTRA_NAME, station.name)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopPlayback(context: Context) {
        val lastStation = _playingStation.value
        _playingStation.value = null
        _playbackState.value = PlaybackState.STOPPED
        if (lastStation != null) {
            updateRecentlyPlayedEndTime(lastStation.stationUuid, System.currentTimeMillis())
        }
        val intent = Intent(context, com.parmarstudios.radiowave.player.RadioPlayerService::class.java).apply {
            action = com.parmarstudios.radiowave.player.RadioPlayerService.ACTION_STOP
        }
        context.startService(intent)
        setElapsedSeconds(0)
    }


    fun onPlaybackStopped() {
        _playingStation.value = null
        _playbackState.value = PlaybackState.STOPPED
    }

    fun closePlayer(context: Context) {
        stopPlayback(context = context)
        _playingStation.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun togglePlayPause(context: Context) {
        when (_playbackState.value) {
            PlaybackState.LOADING -> return
            PlaybackState.PLAYING -> {
                val intent = Intent(context, com.parmarstudios.radiowave.player.RadioPlayerService::class.java).apply {
                    action = com.parmarstudios.radiowave.player.RadioPlayerService.ACTION_PAUSE
                }
                context.startService(intent)
            }
            PlaybackState.STOPPED -> {
                val intent = Intent(context, com.parmarstudios.radiowave.player.RadioPlayerService::class.java).apply {
                    action = com.parmarstudios.radiowave.player.RadioPlayerService.ACTION_RESUME
                }
                context.startService(intent)
            }
        }
    }

    fun onOrderChange(newOrder: String) {
        _order.value = newOrder
        currentPage = 0
        isLastPage = false
        fetchStations(_searchQuery.value, _searchCountry.value, newOrder, _reverse.value)
    }

    fun onReverseToggle() {
        _reverse.value = !_reverse.value
        currentPage = 0
        isLastPage = false
        fetchStations(_searchQuery.value, _searchCountry.value, _order.value, _reverse.value)
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            val uuid = station.stationUuid
            val isFav = favoriteDao.isFavorite(uuid)
            if (isFav) {
                favoriteDao.removeFavorite(FavoriteStation(stationUuid = uuid, name = "", url = "", language = "", country = "", favicon = null, urlResolved = null))
            } else {
                favoriteDao.addFavorite(FavoriteStation.fromRadioStation(station))
            }
            _favoriteStations.value = favoriteDao.getAllFavorites()
        }
    }

    suspend fun isFavorite(uuid: String): Boolean = favoriteDao.isFavorite(uuid)

    fun toggleBlocked(station: RadioStation) {
        viewModelScope.launch {
            val uuid = station.stationUuid
            val isBlocked = blockedDao.isBlocked(uuid)
            if (isBlocked) {
                val allBlocked = blockedDao.getAllBlocked()
                val toRemove = allBlocked.find { it.stationUuid == uuid }
                if (toRemove != null) {
                    blockedDao.removeBlocked(toRemove)
                }
            } else {
                blockedDao.addBlocked(BlockedStation.fromRadioStation(station))
            }
            _blockedStations.value = blockedDao.getAllBlocked()
        }
    }

    fun observeNetwork(context: Context) {
        viewModelScope.launch {
            NetworkMonitor.observe(context).collect { available ->
                _isNetworkAvailable.value = available
                if (available && stations.value.isEmpty()) {
                    fetchStations() // or your fetch method
                }
            }
        }
    }

    fun initApiWithHealthyServer() {
        viewModelScope.launch {
            _uiState.value = StationListUiState.Loading
            // Ensure servers are refreshed before fetching
            healthChecker.refreshHealthyServers()
            val healthyUrl = healthChecker.getHealthyServer()
            if (healthyUrl != null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()
                api = Retrofit.Builder()
                    .baseUrl(healthyUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                    .create(RadioBrowserApi::class.java)
                fetchStations()
            } else {
                _uiState.value = StationListUiState.Error("No healthy server found")
                _stations.value = emptyList()
            }
        }
    }

    fun onPlaybackPaused() {
        _playbackState.value = PlaybackState.STOPPED
    }

    fun onPlaybackResumed() {
        _playbackState.value = PlaybackState.PLAYING
    }

    fun restorePlayingStation(context: Context, station: RadioStation, state: PlaybackState = PlaybackState.STOPPED) {
        _playingStation.value = station
        _playbackState.value = state
        if (state == PlaybackState.PLAYING) {
            playStation(context, station)
        }
    }

    private fun saveLastPlayingStation(
        context: Context,
        station: RadioStation,
        playbackState: PlaybackState,
        elapsedSeconds: Int
    ) {
        val lastPlaying = LastPlayingStation(
            stationUuid = station.stationUuid,
            name = station.name,
            url = station.url,
            urlResolved = station.urlResolved,
            favicon = station.favicon,
            country = station.country,
            language = station.language,
            playbackState = playbackState.name,
            elapsedSeconds = elapsedSeconds
        )
        LastPlayingStationPreference(context).saveStation(lastPlaying)
    }


    fun onPlaybackError(context: Context,msg: String) {
        stopPlayback(context)
        _errorMessage.value = msg
    }

    fun setElapsedSeconds(seconds: Int) {
        _elapsedSeconds.value = seconds
    }

    private fun addRecentlyPlayedStation(station: RadioStation, startTime: Long) {
        viewModelScope.launch {
            val recentlyPlayed = RecentlyPlayedStation.fromRadioStation(station, startTime)
            recentlyPlayedStationDao.insertRecentlyPlayedStation(recentlyPlayed)
        }
    }

    private fun updateRecentlyPlayedEndTime(stationUuid: String, endTime: Long) {
        viewModelScope.launch {
            val list = recentlyPlayedStationDao.getAllRecentlyPlayedStations()
            val last = list.find { it.stationUuid == stationUuid && it.endTime == 0L }
            if (last != null) {
                val updated = last.copy(endTime = endTime)
                recentlyPlayedStationDao.updateRecentlyPlayedStation(updated)
            }
        }
    }

    fun clearRecentlyPlayed() {
        viewModelScope.launch {
            recentlyPlayedStationDao.clearAll()
        }
    }

    fun consumePendingStationToPlay(): RadioStation? {
        val station = pendingStationToPlay
        pendingStationToPlay = null
        return station
    }

    fun clearFromRecent(id: Long) {
        viewModelScope.launch {
            recentlyPlayedStationDao.deleteById(id)
        }
    }

    fun getRecentHistoryForStation(stationUuid: String): List<RecentlyPlayedStation> {
        return recentlyPlayedStations.value.filter { it.stationUuid == stationUuid }
    }

    fun fetchStationByUuid(uuid: String) {
        viewModelScope.launch {
            _stationDetail.value = null
            _stationDetailErrorMessage.value = null
            try {
                val station = getStationByUuid(uuid)
                if (station == null) {
                    _stationDetailErrorMessage.value = "Station not found"
                }
                _stationDetail.value = station
            } catch (e: SocketTimeoutException) {
                _stationDetailErrorMessage.value = "Request timed out. Please try again."
            } catch (e: Exception) {
                _stationDetailErrorMessage.value = "Failed to load station"
            }
        }
    }

    suspend fun getStationByUuid(uuid: String): RadioStation? {
        return api?.getStationByUuid(uuid)?.firstOrNull()
    }

    override fun onCleared() {
        super.onCleared()
        healthChecker.stop();
    }
}