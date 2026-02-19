package com.parmarstudios.radiowave.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.parmarstudios.radiowave.ui.settings.SettingsViewModel
import com.parmarstudios.radiowave.ui.settings.ThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
            navController: NavController,
    bottomContentPadding:Dp,
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel()
    val theme by viewModel.theme.collectAsState()
    val historySize by viewModel.historySize.collectAsState()
    val favoritesSize by viewModel.favoritesSize.collectAsState()
    val blockedSize by viewModel.blockedSize.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = bottomContentPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                ThemeSelector(
                    selected = theme,
                    onSelected = { viewModel.setTheme(it) }
                )
                Spacer(Modifier.height(32.dp))

                Divider()
                Spacer(Modifier.height(16.dp))
                Text("Data Management", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                DataClearRow(
                    label = "Clear History",
                    size = historySize,
                    onClear = {
                        viewModel.clearHistory()
                    }
                )
                DataClearRow(
                    label = "Clear Favorites",
                    size = favoritesSize,
                    onClear = {
                        viewModel.clearFavorites()
                    }
                )
                DataClearRow(
                    label = "Clear Blocked",
                    size = blockedSize,
                    onClear = {
                        viewModel.clearBlocked()
                    }
                )

                Spacer(Modifier.height(32.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("about") }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )

            }
        }
    }
}

@Composable
fun DataClearRow(label: String, size: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClear() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
        Text(size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ThemeSelector(
    selected: ThemeOption,
    onSelected: (ThemeOption) -> Unit
) {
    ThemeOption.values().forEach { option ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .clickable { onSelected(option) }
        ) {
            RadioButton(
                selected = selected == option,
                onClick = null
            )
            Text(option.displayName, modifier = Modifier.padding(start = 8.dp))
        }
    }
}