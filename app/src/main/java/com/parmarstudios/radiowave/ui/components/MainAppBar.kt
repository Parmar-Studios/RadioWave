package com.parmarstudios.radiowave.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    title: String,
    order: String,
    reverse: Boolean,
    onSearchClick: () -> Unit,
    onOrderChange: (String) -> Unit,
    onReverseToggle: () -> Unit,
    onNavIconClick: () -> Unit
) {
    var moreExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = { moreExpanded = true }) {
                Icon(Icons.Default.Sort, contentDescription = "More options")
            }
            DropdownMenu(expanded = moreExpanded, onDismissRequest = { moreExpanded = false }) {
                val sortOptions = listOf("name" to "Name", "country" to "Country", "language" to "Language", "votes" to "Votes")
                sortOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = order == value,
                                    onClick = {
                                        onOrderChange(value)
                                        moreExpanded = false
                                    }
                                )
                                Text(label, modifier = Modifier.padding(start = 8.dp))
                            }
                        },
                        onClick = {
                            onOrderChange(value)
                            moreExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = reverse,
                                onCheckedChange = {
                                    onReverseToggle()
                                    moreExpanded = false
                                }
                            )
                            Text("Reverse order", modifier = Modifier.padding(start = 8.dp))
                        }
                    },
                    onClick = {
                        onReverseToggle()
                        moreExpanded = false
                    }
                )
            }
//            IconButton(onClick = onSettingsClick) {
//                Icon(Icons.Default.Settings, contentDescription = "Settings")
//            }
        }
    )
}