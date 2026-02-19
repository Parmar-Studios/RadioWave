package com.parmarstudios.radiowave.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterChips(
    query: String,
    country: String,
    onClearSearch: () -> Unit,
    onClearCountry: () -> Unit
) {
    Row(modifier = Modifier.padding(8.dp)) {
        if (query.isNotBlank()) {
            AssistChip(
                onClick = {},
                label = { Text("Search: $query") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .width(18.dp)
                            .clickable { onClearSearch() }
                    )
                },
                colors = AssistChipDefaults.assistChipColors()
            )
        }
        if (query.isNotBlank() && country.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
        }
        if (country.isNotBlank()) {
            AssistChip(
                onClick = {},
                label = { Text("Country: $country") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear country",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .width(18.dp)
                            .clickable { onClearCountry() }
                    )
                },
                colors = AssistChipDefaults.assistChipColors()
            )
        }
    }
}