package com.parmarstudios.radiowave.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parmarstudios.radiowave.data.RadioStation

/**
 * Entity representing a recently played radio station in the local Room database.
 *
 * @property id Auto-generated primary key for each entry.
 * @property stationUuid Unique identifier for the station.
 * @property name Name of the radio station.
 * @property url Stream URL of the station.
 * @property urlResolved Resolved stream URL.
 * @property favicon URL to the station's favicon image.
 * @property country Country of the station.
 * @property language Language of the broadcast.
 * @property startTime Timestamp when playback started.
 * @property endTime Timestamp when playback ended.
 * @property changeuuid Unique identifier for changes (optional).
 * @property serveruuid Server unique identifier (optional).
 * @property tags Comma-separated tags (optional).
 * @property countrycode Country code (optional).
 * @property iso_3166_2 ISO 3166-2 code (optional).
 * @property state State or region (optional).
 * @property languagecodes Language codes (optional).
 * @property votes Number of votes (optional).
 * @property lastchangetime Last change time (optional).
 * @property lastchangetime_iso8601 Last change time in ISO8601 (optional).
 * @property codec Audio codec (optional).
 * @property bitrate Bitrate in kbps (optional).
 * @property hls Whether the stream is HLS (optional).
 * @property lastcheckok Last check status (optional).
 * @property lastchecktime Last check time (optional).
 * @property lastchecktime_iso8601 Last check time in ISO8601 (optional).
 * @property lastcheckoktime Last successful check time (optional).
 * @property lastcheckoktime_iso8601 Last successful check time in ISO8601 (optional).
 * @property lastlocalchecktime Last local check time (optional).
 * @property lastlocalchecktime_iso8601 Last local check time in ISO8601 (optional).
 * @property clicktimestamp Last click timestamp (optional).
 * @property clicktimestamp_iso8601 Last click timestamp in ISO8601 (optional).
 * @property clickcount Number of clicks (optional).
 * @property clicktrend Click trend (optional).
 * @property ssl_error SSL error status (optional).
 * @property geo_lat Latitude (optional).
 * @property geo_long Longitude (optional).
 * @property geo_distance Distance from user (optional).
 * @property has_extended_info Whether extended info is available (optional).
 */
@Entity(tableName = "recently_played_station")
data class RecentlyPlayedStation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Auto-generated ID
    val stationUuid: String,                            // Unique station ID
    val name: String?,                                  // Station name
    val url: String?,                                   // Stream URL
    val urlResolved: String?,                           // Resolved stream URL
    val favicon: String?,                               // Favicon URL
    val country: String?,                               // Country
    val language: String?,                              // Broadcast language
    val startTime: Long,                                // Playback start time (epoch millis)
    val endTime: Long,                                  // Playback end time (epoch millis)

    // Optional metadata fields from RadioStation
    val changeuuid: String? = null,
    val serveruuid: String? = null,
    val tags: String? = null,
    val countrycode: String? = null,
    val iso_3166_2: String? = null,
    val state: String? = null,
    val languagecodes: String? = null,
    val votes: Int? = null,
    val lastchangetime: String? = null,
    val lastchangetime_iso8601: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val hls: Int? = null,
    val lastcheckok: Int? = null,
    val lastchecktime: String? = null,
    val lastchecktime_iso8601: String? = null,
    val lastcheckoktime: String? = null,
    val lastcheckoktime_iso8601: String? = null,
    val lastlocalchecktime: String? = null,
    val lastlocalchecktime_iso8601: String? = null,
    val clicktimestamp: String? = null,
    val clicktimestamp_iso8601: String? = null,
    val clickcount: Int? = null,
    val clicktrend: Int? = null,
    val ssl_error: Int? = null,
    val geo_lat: Double? = null,
    val geo_long: Double? = null,
    val geo_distance: Double? = null,
    val has_extended_info: Boolean? = null
) {
    companion object {
        /**
         * Creates a [RecentlyPlayedStation] from a [RadioStation] instance.
         *
         * @param station The [RadioStation] to convert.
         * @param startTime Playback start time in epoch millis.
         * @param endTime Playback end time in epoch millis (default is 0).
         * @return A new [RecentlyPlayedStation] with copied properties.
         */
        fun fromRadioStation(
            station: RadioStation,
            startTime: Long,
            endTime: Long = 0L
        ): RecentlyPlayedStation = RecentlyPlayedStation(
            stationUuid = station.stationUuid,
            name = station.name,
            url = station.url,
            urlResolved = station.urlResolved,
            favicon = station.favicon,
            country = station.country,
            language = station.language,
            startTime = startTime,
            endTime = endTime,
            changeuuid = station.changeuuid,
            serveruuid = station.serveruuid,
            tags = station.tags,
            countrycode = station.countrycode,
            iso_3166_2 = station.iso_3166_2,
            state = station.state,
            languagecodes = station.languagecodes,
            votes = station.votes,
            lastchangetime = station.lastchangetime,
            lastchangetime_iso8601 = station.lastchangetime_iso8601,
            codec = station.codec,
            bitrate = station.bitrate,
            hls = station.hls,
            lastcheckok = station.lastcheckok,
            lastchecktime = station.lastchecktime,
            lastchecktime_iso8601 = station.lastchecktime_iso8601,
            lastcheckoktime = station.lastcheckoktime,
            lastcheckoktime_iso8601 = station.lastcheckoktime_iso8601,
            lastlocalchecktime = station.lastlocalchecktime,
            lastlocalchecktime_iso8601 = station.lastlocalchecktime_iso8601,
            clicktimestamp = station.clicktimestamp,
            clicktimestamp_iso8601 = station.clicktimestamp_iso8601,
            clickcount = station.clickcount,
            clicktrend = station.clicktrend,
            ssl_error = station.ssl_error,
            geo_lat = station.geo_lat,
            geo_long = station.geo_long,
            geo_distance = station.geo_distance,
            has_extended_info = station.has_extended_info
        )
    }
}

/**
 * Extension function to convert a [RecentlyPlayedStation] back to a [RadioStation].
 *
 * @receiver The [RecentlyPlayedStation] instance.
 * @return A [RadioStation] with copied properties.
 */
fun RecentlyPlayedStation.toRadioStation(): RadioStation = RadioStation(
    stationUuid = stationUuid,
    name = name.orEmpty(),
    url = url.orEmpty(),
    urlResolved = urlResolved,
    favicon = favicon,
    country = country.orEmpty(),
    language = language.orEmpty(),
    changeuuid = changeuuid,
    serveruuid = serveruuid,
    tags = tags,
    countrycode = countrycode,
    iso_3166_2 = iso_3166_2,
    state = state,
    languagecodes = languagecodes,
    votes = votes,
    lastchangetime = lastchangetime,
    lastchangetime_iso8601 = lastchangetime_iso8601,
    codec = codec,
    bitrate = bitrate,
    hls = hls,
    lastcheckok = lastcheckok,
    lastchecktime = lastchecktime,
    lastchecktime_iso8601 = lastchecktime_iso8601,
    lastcheckoktime = lastcheckoktime,
    lastcheckoktime_iso8601 = lastcheckoktime_iso8601,
    lastlocalchecktime = lastlocalchecktime,
    lastlocalchecktime_iso8601 = lastlocalchecktime_iso8601,
    clicktimestamp = clicktimestamp,
    clicktimestamp_iso8601 = clicktimestamp_iso8601,
    clickcount = clickcount,
    clicktrend = clicktrend,
    ssl_error = ssl_error,
    geo_lat = geo_lat,
    geo_long = geo_long,
    geo_distance = geo_distance,
    has_extended_info = has_extended_info
)