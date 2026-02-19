package com.parmarstudios.radiowave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.parmarstudios.radiowave.data.RadioStation
import com.parmarstudios.radiowave.R
import com.parmarstudios.radiowave.player.PlaybackState
import com.parmarstudios.radiowave.ui.settings.ThemeOption
import com.parmarstudios.radiowave.ui.util.isAppInDarkTheme

@Composable
fun BottomPlayerBar(
    station: RadioStation,
    onPlayPause: () -> Unit,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
    elapsedSeconds: Int,
    onClose: () -> Unit,
    themeOption: ThemeOption,
    onDetail: () -> Unit
) {
    val isDarkTheme = isAppInDarkTheme(themeOption)
    val placeholderRes = if (isDarkTheme) R.drawable.ic_placeholder_light else R.drawable.ic_placeholder_dark
    val painter = rememberAsyncImagePainter(station.favicon)
    val isError = painter.state is AsyncImagePainter.State.Error || station.favicon.isNullOrEmpty()

    val timerText = remember (elapsedSeconds) {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
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
                        imageVector = Icons.Filled.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name.trim().ifEmpty { "Unknown Station" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listOfNotNull(
                        station.language.takeIf { it.isNotBlank() },
                        station.country.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onPlayPause) {
                when (playbackState) {
                    PlaybackState.PLAYING -> Icon(Icons.Default.Pause, contentDescription = "Pause")
                    PlaybackState.STOPPED -> Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    PlaybackState.LOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
            IconButton(onClick = onDetail) { // Add this block
                Icon(Icons.Outlined.Info, contentDescription = "Details")
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}