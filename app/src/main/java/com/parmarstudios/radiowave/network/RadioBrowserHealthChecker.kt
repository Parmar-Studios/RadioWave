package com.parmarstudios.radiowave.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parmarstudios.radiowave.data.db.AppDatabase
import com.parmarstudios.radiowave.data.db.Server
import com.parmarstudios.radiowave.data.db.ServerDao
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Class responsible for periodically checking the health of RadioBrowser servers.
 * Stores server health status and scores in the local Room database.
 *
 * @property client OkHttpClient used for network requests (with timeouts).
 * @property checkIntervalMillis Interval between periodic health checks in milliseconds.
 */
class RadioBrowserHealthChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val checkIntervalMillis: Long = 5 * 60 * 1000L // Default: 5 minutes
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO) // Coroutine scope for background tasks
    private val TAG = "RadioBrowserHealthChecker"
    private lateinit var serverDao: ServerDao // DAO for server database operations

    init {
        // Initialize the database and DAO, then start periodic health checks
        val context = com.parmarstudios.radiowave.RadioWaveApp.appContext()
        val db = AppDatabase.getInstance(context)
        serverDao = db.serverDao()
        startPeriodicHealthCheck()
    }

    /**
     * Returns the URL of a healthy server, or null if none are healthy.
     */
    suspend fun getHealthyServer(): String? = withContext(Dispatchers.IO) {
        val servers = serverDao.getHealthyServers()
        for (server in servers) {
            if (server.isHealthy) return@withContext server.url // Return first healthy server
        }
        null // No healthy server found
    }

    /**
     * Refreshes the health status of all known servers by checking their health endpoints.
     *
     * @param path The health check path to use (default: "/json/stats").
     * @param retries Number of retries for each server.
     * @param retryDelayMillis Delay between retries in milliseconds.
     */
    suspend fun refreshHealthyServers(
        path: String = "/json/stats",
        retries: Int = 3,
        retryDelayMillis: Long = 1000L
    ) = withContext(Dispatchers.IO) {
        val servers = fetchServerNames() // Get list of server names from API
        for (serverName in servers) {
            val url = "https://$serverName"
            // Insert server into DB if not present
            serverDao.insert(Server(url = url, isHealthy = false, score = 0))
            // Check server health and update DB accordingly
            if (isServerHealthy(url, path, retries, retryDelayMillis)) {
                serverDao.updateHealthAndIncrementScore(url, true)
            } else {
                serverDao.updateHealthAndDecrementScore(url, false)
            }
        }
    }

    /**
     * Checks if a server is healthy by making a request to its health endpoint.
     * Retries the request if it fails.
     *
     * @param url The base URL of the server.
     * @param path The health check path.
     * @param retries Number of retries.
     * @param retryDelayMillis Delay between retries.
     * @return True if the server is healthy, false otherwise.
     */
    private suspend fun isServerHealthy(
        url: String,
        path: String,
        retries: Int,
        retryDelayMillis: Long
    ): Boolean {
        repeat(retries) {
            try {
                val request = Request.Builder().url("$url$path").build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) return true // Server is healthy
                }
            } catch (_: Exception) {
                // Ignore and retry
            }
            delay(retryDelayMillis) // Wait before retrying
        }
        return false // All retries failed
    }

    /**
     * Fetches the list of RadioBrowser server names from the public API.
     *
     * @return List of server names, or empty list on failure.
     */
    private suspend fun fetchServerNames(): List<String> = withContext(Dispatchers.IO) {
        var attempts = 0
        var servers: List<String> = emptyList()
        while (attempts < 4 && servers.isEmpty()) {
            val request = Request.Builder()
                .url("https://all.api.radio-browser.info/json/servers")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val type = object : TypeToken<List<Map<String, String>>>() {}.type
                        val serverList: List<Map<String, String>> = Gson().fromJson(body, type)
                        servers = serverList.mapNotNull { it["name"] }.distinct()
                    }
                }
            } catch (_: Exception) {
                // Ignore and retry
            }
            if (servers.isEmpty()) delay(1000L)
            attempts++
        }
        servers
    }

    /**
     * Starts a coroutine that periodically refreshes the health status of all servers.
     */
    private fun startPeriodicHealthCheck() {
        scope.launch {
            while (isActive) {
                refreshHealthyServers() // Perform health check
                delay(checkIntervalMillis) // Wait for next interval
            }
        }
    }

    /**
     * Stops the periodic health check coroutine.
     */
    fun stop() {
        scope.cancel()
    }
}