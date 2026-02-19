package com.parmarstudios.radiowave.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a radio server in the local Room database.
 *
 * @property url The unique URL of the server (primary key).
 * @property isHealthy Indicates if the server is currently healthy.
 * @property score Health or performance score for the server (default is 0).
 */
@Entity(tableName = "servers")
data class Server(
    @PrimaryKey val url: String,      // Unique server URL (primary key)
    val isHealthy: Boolean,           // Server health status
    val score: Int = 0                // Server score (default 0)
)