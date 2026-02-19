package com.parmarstudios.radiowave.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val drawerItems = listOf(
    DrawerItem("All Stations", Icons.Default.Home, "radio"),
    DrawerItem("Favorite Stations", Icons.Default.Favorite, "favorites"),
    DrawerItem("Blocked Stations", Icons.Default.Block, "blocked"),
    DrawerItem("History", Icons.Default.History, "history"),
    DrawerItem("Settings", Icons.Default.Settings, "settings")
)

@Composable
fun NavigationDrawerContent(
    selectedItem: Int,
    modifier: Modifier = Modifier,
    onDrawerItemClick: suspend (Int, String) -> Unit,

) {
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "RadioWave",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            softWrap = false,
        )

        Divider(Modifier.padding(vertical = 16.dp))

        drawerItems.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = { androidx.compose.material3.Text(item.label) },
                selected = selectedItem == index,
                onClick = {
                    coroutineScope.launch {
                        onDrawerItemClick(index, item.route)
                    }
//                    onItemSelected(index)
//                    onNavigate(item.route)
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@Composable
fun RadioWaveNavigationDrawer(
    drawerState: DrawerState,
    selectedItem: Int,
    onDrawerItemClick: suspend (Int, String) -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                selectedItem = selectedItem,
                onDrawerItemClick = onDrawerItemClick
            )
        },
        content = content
    )
}