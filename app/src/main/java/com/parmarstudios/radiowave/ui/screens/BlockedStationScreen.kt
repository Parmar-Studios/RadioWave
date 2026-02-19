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
fun BlockedStationScreen(
    viewModel: RadioStationsViewModel,
    navController: NavController,
    bottomContentPadding: Dp,
    onBack: () -> Unit
) {
    val blockedStations by viewModel.blockedStations.collectAsState()
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
        "language" to "Language"
    )

    // Filter and sort blocked stations
    val filteredBlocked = blockedStations
        .filter { blocked ->
            val station = blocked.toRadioStation()
            (searchQuery.isBlank() || (station.name?.contains(searchQuery, ignoreCase = true) == true)) &&
                    (searchCountry.isBlank() || (station.country?.equals(searchCountry, ignoreCase = true) == true))
        }
        .let { list ->
            when (order) {
                "name" -> if (reverse) list.sortedByDescending { it.name?.lowercase() ?: "" } else list.sortedBy { it.name?.lowercase() ?: "" }
                "country" -> if (reverse) list.sortedByDescending { it.country?.lowercase() ?: "" } else list.sortedBy { it.country?.lowercase() ?: "" }
                "language" -> if (reverse) list.sortedByDescending { it.language?.lowercase() ?: "" } else list.sortedBy { it.language?.lowercase() ?: "" }
                else -> list
            }
        }

    LaunchedEffect(Unit) {
        viewModel.initBlocked()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Stations") },
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
            if (filteredBlocked.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No blocked stations found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBlocked) { blocked ->
                        RadioStationListItem(
                            station = blocked.toRadioStation(),
                            disablePlayPause = true,
                            themeOption = theme,
                            onBlock = { viewModel.toggleBlocked(blocked.toRadioStation()) },
                            isBlocked = true,
                            onDetails = { navController.navigate("station_detail/${blocked.stationUuid}") }
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