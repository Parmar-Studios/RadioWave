package com.parmarstudios.radiowave

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.compose.RadioWaveTheme
import com.parmarstudios.radiowave.data.RadioStation
import com.parmarstudios.radiowave.player.RadioPlayerService
import com.parmarstudios.radiowave.ui.screens.MainScreen
import com.parmarstudios.radiowave.ui.settings.SettingsViewModel
import com.parmarstudios.radiowave.ui.settings.ThemeOption
import com.parmarstudios.radiowave.viewmodel.RadioStationsViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.parmarstudios.radiowave.data.preferences.LastPlayingStationPreference
import com.parmarstudios.radiowave.player.PlaybackState


class MainActivity : ComponentActivity() {
    
    private val radioStationsViewModel: RadioStationsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RadioStationsViewModel(applicationContext) as T
            }
        }
    }

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                RadioPlayerService.ACTION_PLAYBACK_STOPPED -> {
                    radioStationsViewModel.onPlaybackStopped()
                    val pending = radioStationsViewModel.consumePendingStationToPlay()
                    if (pending != null && context != null) {
                        radioStationsViewModel.playStation(context, pending)
                    }
                }
                RadioPlayerService.ACTION_PLAYBACK_PAUSED -> {
                    radioStationsViewModel.onPlaybackPaused()
                }
                RadioPlayerService.ACTION_PLAYBACK_RESUMED -> {
                    radioStationsViewModel.onPlaybackResumed()
                }
                RadioPlayerService.ACTION_PLAYBACK_ERROR -> {
                    val errorMsg = intent.getStringExtra(RadioPlayerService.EXTRA_ERROR_MESSAGE) ?: "Playback error"
                    radioStationsViewModel.onPlaybackError(this@MainActivity, errorMsg)
                }
                RadioPlayerService.ACTION_PLAYBACK_TIMER -> {
                    val seconds = intent.getIntExtra(RadioPlayerService.EXTRA_ELAPSED_SECONDS, 0)
                    radioStationsViewModel.setElapsedSeconds(seconds)
                }
            }
        }
    }

    private var showPermissionDeniedSnackbar by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showPermissionDeniedSnackbar = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Restore last playing station if it exists
        val lastPlayingPref = LastPlayingStationPreference(this)
        val lastStation = lastPlayingPref.getStation()
        if (lastStation != null) {
            radioStationsViewModel.restorePlayingStation(
                this,
                RadioStation(
                    stationUuid = lastStation.stationUuid,
                    name = lastStation.name,
                    url = lastStation.url,
                    urlResolved = lastStation.urlResolved,
                    favicon = lastStation.favicon,
                    country = lastStation.country,
                    language = lastStation.language
                ),
                PlaybackState.valueOf(lastStation.playbackState)
            )
            radioStationsViewModel.setElapsedSeconds(lastStation.elapsedSeconds)
        }

        // Register broadcast receivers
        val filter = IntentFilter().apply {
            addAction(RadioPlayerService.ACTION_PLAYBACK_STOPPED)
            addAction(RadioPlayerService.ACTION_PLAYBACK_PAUSED)
            addAction(RadioPlayerService.ACTION_PLAYBACK_RESUMED)
            addAction(RadioPlayerService.ACTION_PLAYBACK_ERROR)
            addAction(RadioPlayerService.ACTION_PLAYBACK_TIMER)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(playbackReceiver, filter)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val theme by settingsViewModel.theme.collectAsState()
            val darkTheme = when (theme) {
                ThemeOption.LIGHT -> false
                ThemeOption.DARK -> true
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }
            val snackbarHostState = remember { SnackbarHostState() }

            // Show snackbar if permission denied
            LaunchedEffect(showPermissionDeniedSnackbar) {
                if (showPermissionDeniedSnackbar) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Notification permission denied. You can enable it in app settings.",
                        actionLabel = "Settings"
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    showPermissionDeniedSnackbar = false
                }
            }

            RadioWaveTheme(darkTheme = darkTheme) {
                val view = LocalView.current
                val context = LocalContext.current
                val colorScheme = MaterialTheme.colorScheme
                SideEffect {
                    val window = (context as? Activity)?.window ?: return@SideEffect
                    window.navigationBarColor = colorScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, view)
                        .isAppearanceLightNavigationBars = !darkTheme
                }

                val navController = rememberNavController()
                val statusBarModifier = if (Build.VERSION.SDK_INT < 35) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars)
                } else {
                    Modifier
                }
                MainScreen(navController = navController, modifier = statusBarModifier, viewModel = radioStationsViewModel)
                SnackbarHost(
                    hostState = snackbarHostState
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackReceiver)
    }
}