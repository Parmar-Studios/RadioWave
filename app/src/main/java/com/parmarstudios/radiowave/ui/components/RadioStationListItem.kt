package com.parmarstudios.radiowave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.parmarstudios.radiowave.data.RadioStation
import com.parmarstudios.radiowave.R
import com.parmarstudios.radiowave.ui.settings.ThemeOption
import com.parmarstudios.radiowave.ui.util.isAppInDarkTheme

@Composable
fun RadioStationListItem(
    station: RadioStation,
    onClick: () -> Unit = {},
    onDetails: () -> Unit = {},
    onClearFromRecent: () -> Unit = {},
    onBlock: () -> Unit = {},
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    showFavorite: Boolean = true,
    showBlock: Boolean = true,
    showClear: Boolean = false,
    isBlocked: Boolean = false,
    disablePlayPause: Boolean = false,
    themeOption: ThemeOption,
    recentStartTime: Long? = null,
    recentEndTime: Long? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDarkTheme = isAppInDarkTheme(themeOption)
    val placeholderRes = if (isDarkTheme) R.drawable.ic_placeholder_light else R.drawable.ic_placeholder_dark

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(enabled = !disablePlayPause) { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberAsyncImagePainter(station.favicon)
            val isError = painter.state is AsyncImagePainter.State.Error || station.favicon.isNullOrEmpty()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isError) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Radio, // Use a suitable icon from material icons
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = station.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Unknown Station",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                val language = station.language?.takeIf { it.isNotBlank() }
                val country = station.country?.takeIf { it.isNotBlank() }
                val info = when {
                    country == null && language == null  -> "Country: N/A    Language: N/A"
                    country != null && language == null  -> "Country: $country    Language: N/A"
                    country == null && language != null  -> "Country: N/A    Language: $language"
                    else -> "Country: $country   Language: $language"
                }
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (recentStartTime != null) {
                    val context = LocalContext.current
                    val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
                    val start = timeFormat.format(java.util.Date(recentStartTime))
                    val playedText = if (recentEndTime != null && recentEndTime != 0L) {
                        val end = timeFormat.format(java.util.Date(recentEndTime))
                        "$start–$end"
                    } else {
                        "$start–Ongoing"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Played time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = playedText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (showFavorite) {
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "Remove from Favorite" else "Add to Favorite") },
                            onClick = {
                                menuExpanded = false
                                onToggleFavorite()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            menuExpanded = false
                            onDetails()
                        }
                    )
                    if (showBlock) {
                        DropdownMenuItem(
                            text = { Text(if (isBlocked) "Unblock" else "Block") },
                            onClick = {
                                menuExpanded = false
                                onBlock()
                            }
                        )
                    }
                    if (showClear) {
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            onClick = {
                                menuExpanded = false
                                onClearFromRecent()
                            }
                        )
                    }
                }
            }
        }
    }
}