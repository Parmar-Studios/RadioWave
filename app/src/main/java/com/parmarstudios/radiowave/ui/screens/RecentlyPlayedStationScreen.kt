package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedStationScreen(
    viewModel: RadioStationsViewModel,
    navController: NavController,
    onBack: () -> Unit,
    bottomContentPadding: Dp
) {
    val recentlyPlayedStations by viewModel.recentlyPlayedStations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchCountry by viewModel.searchCountry.collectAsState()
    val order by viewModel.order.collectAsState()
    val reverse by viewModel.reverse.collectAsState()
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel()
    val theme by settingsViewModel.theme.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }

    // Filter and sort recently played stations
    val filteredRecentlyPlayed = recentlyPlayedStations
        .filter { station ->
            val radio = station.toRadioStation()
            (searchQuery.isBlank() || (radio.name?.contains(searchQuery, ignoreCase = true) == true)) &&
                    (searchCountry.isBlank() || (radio.country?.equals(searchCountry, ignoreCase = true) == true))
        }
        .let { list ->
            when (order) {
                "name" -> if (reverse) list.sortedByDescending { it.name?.lowercase() ?: "" } else list.sortedBy { it.name?.lowercase() ?: "" }
                "country" -> if (reverse) list.sortedByDescending { it.country?.lowercase() ?: "" } else list.sortedBy { it.country?.lowercase() ?: "" }
                "language" -> if (reverse) list.sortedByDescending { it.language?.lowercase() ?: "" } else list.sortedBy { it.language?.lowercase() ?: "" }
                else -> list
            }
        }

    // Helper to get date label
    fun getDateLabel(date: Date): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val cal = Calendar.getInstance().apply { time = date }
        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            else -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        }
    }

    // Helper to get a comparable key for sorting
    fun getSortKey(label: String): Long {
        return when (label) {
            "Today" -> Long.MAX_VALUE
            "Yesterday" -> Long.MAX_VALUE - 1
            else -> {
                try {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = sdf.parse(label)
                    date?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }

    // Group by date (using startTime)
    val grouped = filteredRecentlyPlayed
            .groupBy { station ->
        val date = Date(station.startTime)
        getDateLabel(date)
    }
        .mapValues { entry -> entry.value.sortedByDescending { it.startTime } }
        .toSortedMap(compareByDescending { label -> getSortKey(label) })

    LaunchedEffect(Unit) {
        viewModel.initRecentlyPlayed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Played Stations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(
                        onClick = { viewModel.clearRecentlyPlayed() },
                        enabled = recentlyPlayedStations.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All")
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
            if (filteredRecentlyPlayed.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recently played stations found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    grouped.forEach { (dateLabel, stations) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                        items(stations) { station ->
                            RadioStationListItem(
                                station = station.toRadioStation(),
                                themeOption = theme,
                                onClick = { viewModel.playStation(context, station.toRadioStation()) },
                                recentStartTime = station.startTime,
                                recentEndTime = station.endTime,
                                showBlock = false,
                                showFavorite = false,
                                showClear = true,
                                onDetails = { navController.navigate("station_detail/${station.stationUuid}") },
                                onClearFromRecent = { viewModel.clearFromRecent(station.id) }
                            )
                        }
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