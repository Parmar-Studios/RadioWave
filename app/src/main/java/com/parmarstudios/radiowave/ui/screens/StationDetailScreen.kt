package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.parmarstudios.radiowave.data.RadioStation
import com.parmarstudios.radiowave.data.db.RecentlyPlayedStation
import com.parmarstudios.radiowave.viewmodel.RadioStationsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    uuid: String,
    onBack: () -> Unit,
    viewModel: RadioStationsViewModel,
    bottomContentPadding: Dp
) {
    val context = LocalContext.current
    val station by viewModel.stationDetail.collectAsState()
    val errorMessage by viewModel.stationDetailErrorMessage.collectAsState()
    val recentHistoryState = viewModel.recentlyPlayedStations.collectAsState(emptyList())

    LaunchedEffect(uuid) {
        viewModel.fetchStationByUuid(uuid)
    }

    val filteredHistory = remember(recentHistoryState.value, station?.stationUuid) {
        station?.let { s -> recentHistoryState.value.filter { it.stationUuid == s.stationUuid } } ?: emptyList()
    }

    StationDetailContent(
        station = station,
        errorMessage = errorMessage,
        onBack = onBack,
        viewModel = viewModel,
        bottomContentPadding = bottomContentPadding,
        filteredHistory = filteredHistory,
        context = context,
        onRetry = { viewModel.fetchStationByUuid(uuid) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailContent(
    station: RadioStation?,
    errorMessage: String?,
    onBack: () -> Unit,
    viewModel: RadioStationsViewModel,
    bottomContentPadding: Dp,
    filteredHistory: List<RecentlyPlayedStation>,
    context: android.content.Context,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = station?.name ?: "Station Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (station != null) {
                        IconButton(onClick = { viewModel.playStation(context, station) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
                station == null -> {
                    CircularProgressIndicator()
                }
                else -> {
                    StationDetailBody(
                        station = station,
                        bottomContentPadding = bottomContentPadding,
                        filteredHistory = filteredHistory
                    )
                }
            }
        }
    }
}

@Composable
private fun StationDetailBody(
    station: RadioStation,
    bottomContentPadding: Dp,
    filteredHistory: List<RecentlyPlayedStation>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomContentPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val painter = rememberAsyncImagePainter(station.favicon)
                val isError = station.favicon.isNullOrEmpty()
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isError) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = station.name.ifBlank { "Unknown Station" },
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = station.country.ifBlank { "N/A" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = station.language.ifBlank { "N/A" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Stream URL:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = station.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!station.urlResolved.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Resolved URL:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = station.urlResolved ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                DetailRow(label = "Codec", value = station.codec)
                DetailRow(label = "Bitrate", value = station.bitrate?.let { "$it kbps" })
                DetailRow(label = "Votes", value = station.votes?.toString())
                DetailRow(label = "Tags", value = station.tags?.takeIf { it.isNotBlank() })
                DetailRow(label = "State", value = station.state?.takeIf { it.isNotBlank() })
                DetailRow(label = "Last Change", value = station.lastchangetime)
                DetailRow(label = "HLS", value = station.hls?.let { if (it == 1) "Yes" else "No" })
                DetailRow(label = "Country Code", value = station.countrycode)
                DetailRow(label = "Language Codes", value = station.languagecodes)
                DetailRow(label = "ISO 3166-2", value = station.iso_3166_2)
                DetailRow(label = "Last Check OK", value = station.lastcheckok?.let { if (it == 1) "Yes" else "No" })
                DetailRow(label = "Last Check Time", value = station.lastchecktime)
                DetailRow(label = "Click Count", value = station.clickcount?.toString())
                DetailRow(label = "Click Trend", value = station.clicktrend?.toString())
                DetailRow(label = "SSL Error", value = station.ssl_error?.toString())
                DetailRow(label = "Geo Lat", value = station.geo_lat?.toString())
                DetailRow(label = "Geo Long", value = station.geo_long?.toString())
                DetailRow(label = "Geo Distance", value = station.geo_distance?.toString())
                DetailRow(label = "Has Extended Info", value = station.has_extended_info?.let { if (it) "Yes" else "No" })
            }
        }

        if (filteredHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recent History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RecentHistoryCard(filteredHistory)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentHistoryCard(historyList: List<RecentlyPlayedStation>) {
    if (historyList.isEmpty()) return

    val grouped = historyList.groupBy {
        val date = Date(it.startTime)
        android.text.format.DateFormat.format("yyyy-MM-dd", date).toString()
    }

    val todayStr = android.text.format.DateFormat.format("yyyy-MM-dd", Date()).toString()
    val dateHeaderFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column {
        grouped.forEach { (dateStr, sessions) ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            val isToday = dateStr == todayStr
            Text(
                text = if (isToday) "Today" else dateHeaderFormat.format(date!!),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            sessions.forEach { HistoryRow(it) }
        }
    }
}

@Composable
private fun HistoryRow(history: RecentlyPlayedStation) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val start = timeFormat.format(Date(history.startTime))
    val end = if (history.endTime > 0) timeFormat.format(Date(history.endTime)) else "Ongoing"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = start, style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Stop, contentDescription = "End", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = end, style = MaterialTheme.typography.bodySmall)
        }
    }
}