package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.parmarstudios.radiowave.ui.components.*
import com.parmarstudios.radiowave.ui.settings.SettingsViewModel
import com.parmarstudios.radiowave.viewmodel.RadioStationsViewModel
import com.parmarstudios.radiowave.viewmodel.StationListUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioStationScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: RadioStationsViewModel,
    bottomContentPadding: Dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val context = LocalContext.current

    val order by viewModel.order.collectAsState()
    val reverse by viewModel.reverse.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchCountry by viewModel.searchCountry.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }

    val settingsViewModel: SettingsViewModel = viewModel()
    val theme by settingsViewModel.theme.collectAsState()

    // Drawer state and selection
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var selectedItem by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val favoriteStations by viewModel.favoriteStations.collectAsState()
    val blockedStations by viewModel.blockedStations.collectAsState()

    // Observe network state and DAOs
    LaunchedEffect(Unit) {
        viewModel.observeNetwork(context)
        viewModel.initFavorites()
        viewModel.initBlocked()
        viewModel.initRecentlyPlayed()
    }

    if (showSearchDialog) {
        SearchDialog(
            initialQuery = searchQuery,
            initialCountry = searchCountry,
            onSearch = { query, country ->
                viewModel.onSearchInputChange(query, country)
            },
            onDismiss = { showSearchDialog = false }
        )
    }

    RadioWaveNavigationDrawer(
        drawerState = drawerState,
        selectedItem = selectedItem,
        onDrawerItemClick = { index, route ->
            selectedItem = index
            drawerState.close() // suspend until closed
            navController.navigate(route)
        }
    ) {
        Scaffold(
            topBar = {
                MainAppBar(
                    title = "RadioWave",
                    order = order,
                    reverse = reverse,
                    onSearchClick = { showSearchDialog = true },
                    onOrderChange = { viewModel.onOrderChange(it) },
                    onReverseToggle = { viewModel.onReverseToggle() },
                    onNavIconClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = bottomContentPadding)
            ) {
                when {
                    !isNetworkAvailable -> {
                        // Centered network error message
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "No network",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .size(64.dp)
                            )
                            Text(
                                text = "No internet connection",
                                color = Color.Gray,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Please check your network settings.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    uiState is StationListUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState is StationListUiState.Error -> {
                        val message = (uiState as StationListUiState.Error).message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message,
                                color = Color.Red,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    uiState is StationListUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No stations found.",
                                color = Color.Gray,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    uiState is StationListUiState.Success -> {
                        val stations = (uiState as StationListUiState.Success).stations
                        // Filter out blocked stations
                        val visibleStations = stations.filter { station ->
                            blockedStations.none { it.stationUuid == station.stationUuid }
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            FilterChips(
                                query = searchQuery,
                                country = searchCountry,
                                onClearSearch = { viewModel.onSearchInputChange("", searchCountry) },
                                onClearCountry = { viewModel.onSearchInputChange(searchQuery, "") }
                            )

                            LazyColumn {
                                itemsIndexed(visibleStations) { index, station ->
                                    val isFavorite = favoriteStations.any { it.stationUuid == station.stationUuid }
                                    RadioStationListItem(
                                        station = station,
                                        isFavorite = isFavorite,
                                        onBlock = { viewModel.toggleBlocked(station) },
                                        onClick = { viewModel.playStation(context, station) },
                                        themeOption = theme,
                                        onToggleFavorite = { viewModel.toggleFavorite(station) },
                                        onDetails = { navController.navigate("station_detail/${station.stationUuid}") }
                                    )

                                    // Trigger load more when 5 items from the end
                                    if (index == visibleStations.lastIndex - 5) {
                                        viewModel.loadMoreStations()
                                    }
                                }
                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}