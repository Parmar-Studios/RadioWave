package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.parmarstudios.radiowave.data.db.toRadioStation
import com.parmarstudios.radiowave.ui.components.RadioStationListItem
import com.parmarstudios.radiowave.ui.components.SearchDialog
import com.parmarstudios.radiowave.ui.components.FilterChips
import com.parmarstudios.radiowave.ui.settings.SettingsViewModel
import com.parmarstudios.radiowave.viewmodel.RadioStationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteStationScreen(
    viewModel: RadioStationsViewModel,
    onBack: () -> Unit,
    navController: NavController,
    bottomContentPadding: Dp
) {
    val favoriteStations by viewModel.favoriteStations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchCountry by viewModel.searchCountry.collectAsState()
    val order by viewModel.order.collectAsState()
    val reverse by viewModel.reverse.collectAsState()
    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = viewModel()
    val theme by settingsViewModel.theme.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortOptions = listOf(
        "name" to "Name",
        "country" to "Country",
        "language" to "Language",
        //"votes" to "Votes"
    )
    val blockedStations by viewModel.blockedStations.collectAsState()

    // Filter and sort favorite stations
    val filteredFavorites = favoriteStations
        .filter { fav ->
            val station = fav.toRadioStation()
            blockedStations.none { it.stationUuid == fav.stationUuid } &&
                    (searchQuery.isBlank() || (station.name?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true)) &&
                    (searchCountry.isBlank() || (station.country?.equals(
                        searchCountry,
                        ignoreCase = true
                    ) == true))
        }
        .let { list ->
            when (order) {
                "name" -> if (reverse) list.sortedByDescending {
                    it.name?.lowercase() ?: ""
                } else list.sortedBy { it.name?.lowercase() ?: "" }

                "country" -> if (reverse) list.sortedByDescending {
                    it.country?.lowercase() ?: ""
                } else list.sortedBy { it.country?.lowercase() ?: "" }

                "language" -> if (reverse) list.sortedByDescending {
                    it.language?.lowercase() ?: ""
                } else list.sortedBy { it.language?.lowercase() ?: "" }
                //"votes" -> if (reverse) list.sortedByDescending { it.votes ?: 0 } else list.sortedBy { it.votes ?: 0 }
                else -> list
            }
        }

    LaunchedEffect(Unit) {
        viewModel.initFavorites()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Stations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            sortOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = order == value,
                                                onClick = {
                                                    viewModel.onOrderChange(value)
                                                    showSortMenu = false
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(label)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onOrderChange(value)
                                        showSortMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = reverse,
                                            onCheckedChange = {
                                                viewModel.onReverseToggle()
                                                showSortMenu = false
                                            }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Reverse order")
                                    }
                                },
                                onClick = {
                                    viewModel.onReverseToggle()
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = bottomContentPadding)
        ) {
            FilterChips(
                query = searchQuery,
                country = searchCountry,
                onClearSearch = { viewModel.onSearchInputChange("", searchCountry) },
                onClearCountry = { viewModel.onSearchInputChange(searchQuery, "") }
            )
            Spacer(Modifier.height(4.dp))
            if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorite stations found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFavorites) { fav ->
                        RadioStationListItem(
                            station = fav.toRadioStation(),
                            isFavorite = true,
                            onClick = { viewModel.playStation(context, fav.toRadioStation()) },
                            onToggleFavorite = { viewModel.toggleFavorite(fav.toRadioStation()) },
                            themeOption = theme,
                            onBlock = {
                                viewModel.toggleBlocked(fav.toRadioStation())
                                // Immediately refresh favorites after blocking
                                viewModel.initFavorites()
                            },
                            onDetails = { navController.navigate("station_detail/${fav.stationUuid}") } // <-- Add this

                        )
                    }
                }
            }
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
    }
}