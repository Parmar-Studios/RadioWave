package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.parmarstudios.radiowave.ui.components.BottomPlayerBar
import com.parmarstudios.radiowave.ui.settings.SettingsViewModel
import com.parmarstudios.radiowave.ui.util.UpdateStatusBarTheme
import com.parmarstudios.radiowave.viewmodel.RadioStationsViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun MainScreen(navController: NavHostController, modifier: Modifier = Modifier, viewModel: RadioStationsViewModel) {

    val playingStation by viewModel.playingStation.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val settingsViewModel: SettingsViewModel = viewModel()
    val theme by settingsViewModel.theme.collectAsState()

    val playerBarHeight = 80.dp // Match your BottomPlayerBar's height
    val bottomContentPadding = if (playingStation != null) playerBarHeight else 0.dp

    UpdateStatusBarTheme(theme)
    // Show snackbar when errorMessage changes
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = "radio",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("radio") {
                RadioStationScreen(
                    navController = navController,
                    viewModel = viewModel,
                    bottomContentPadding = bottomContentPadding,
            )
            }
            composable("favorites") {
                FavoriteStationScreen(
                    viewModel = viewModel,
                    bottomContentPadding = bottomContentPadding,
                    navController = navController,
                    onBack = { navController.navigateUp() }
                )
            }
            composable("blocked") {
                BlockedStationScreen(
                    viewModel = viewModel,
                    bottomContentPadding = bottomContentPadding,
                    navController = navController,
                    onBack = { navController.navigateUp() }
                )
            }
            composable("history") {
                RecentlyPlayedStationScreen(
                    viewModel = viewModel,
                    bottomContentPadding = bottomContentPadding,
                    navController = navController,
                    onBack = { navController.navigateUp() }
                )
            }

            composable(
                "station_detail/{stationUuid}",
                arguments = listOf(navArgument("stationUuid") { type = NavType.StringType })
            ) { backStackEntry ->
                val stationUuid = backStackEntry.arguments?.getString("stationUuid")
                if (stationUuid != null) {
                    StationDetailScreen(
                        uuid = stationUuid,
                        viewModel = viewModel,
                        bottomContentPadding = bottomContentPadding,
                        onBack = { navController.navigateUp() }
                    )
                }
            }

            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    bottomContentPadding = bottomContentPadding,
                    onBack = { navController.navigateUp() }
                )
            }

            composable("about") {
                AboutAppScreen(
                    bottomContentPadding = bottomContentPadding,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        playingStation?.let { station ->
            BottomPlayerBar(
                station = station,
                onPlayPause = { viewModel.togglePlayPause(context) },
                playbackState = playbackState,
                onClose = { viewModel.closePlayer(context) },
                elapsedSeconds = elapsedSeconds,
                themeOption = theme,
                onDetail = {
                    val route = "station_detail/${station.stationUuid}"
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo(route) { inclusive = false }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding()
            )
        }
        // SnackbarHost at the bottom, above the BottomPlayerBar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}