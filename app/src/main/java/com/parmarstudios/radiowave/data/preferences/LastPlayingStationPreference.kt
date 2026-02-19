package com.parmarstudios.radiowave.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.parmarstudios.radiowave.player.PlaybackState

data class LastPlayingStation(
    val stationUuid: String,
    val name: String,
    val url: String,
    val urlResolved: String?,
    val favicon: String?,
    val country: String,
    val language: String,
    val playbackState: String,
    val elapsedSeconds: Int
)

class LastPlayingStationPreference(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("radio_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "last_playing_station"

    fun saveStation(station: LastPlayingStation) {
        val json = gson.toJson(station)
        prefs.edit().putString(key, json).apply()
    }

    fun getStation(): LastPlayingStation? {
        val json = prefs.getString(key, null) ?: return null
        return gson.fromJson(json, LastPlayingStation::class.java)
    }

    fun isStationSaved(stationUuid: String): Boolean {
        val saved = getStation()
        return saved?.stationUuid == stationUuid
    }

    fun updatePlaybackState(state: PlaybackState) {
        val last = getStation() ?: return
        saveStation(last.copy(playbackState = state.name))
    }

    fun updateElapsedSeconds(seconds: Int) {
        val last = getStation() ?: return
        saveStation(last.copy(elapsedSeconds = seconds))
    }

    fun clearStation() {
        prefs.edit().remove(key).apply()
    }
}