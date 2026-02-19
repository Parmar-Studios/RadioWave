package com.parmarstudios.radiowave.data

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a radio station, typically received from a remote API.
 *
 * @property stationUuid Unique identifier for the station.
 * @property name Name of the radio station.
 * @property url Stream URL of the station.
 * @property language Language of the broadcast.
 * @property country Country where the station is based.
 * @property favicon URL to the station's favicon image (optional).
 * @property urlResolved Resolved stream URL (optional).
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
data class RadioStation(
    @SerializedName("stationuuid")
    val stationUuid: String, // Unique station ID

    @SerializedName("name")
    val name: String, // Station name

    @SerializedName("url")
    val url: String, // Stream URL

    @SerializedName("language")
    val language: String, // Broadcast language

    @SerializedName("country")
    val country: String, // Country

    @SerializedName("favicon")
    val favicon: String?, // Favicon URL (optional)

    @SerializedName("url_resolved")
    val urlResolved: String?, // Resolved stream URL (optional)

    // Optional metadata fields
    @SerializedName("changeuuid")
    val changeuuid: String? = null, // Change UUID

    @SerializedName("serveruuid")
    val serveruuid: String? = null, // Server UUID

    @SerializedName("tags")
    val tags: String? = null, // Comma-separated tags

    @SerializedName("countrycode")
    val countrycode: String? = null, // Country code

    @SerializedName("iso_3166_2")
    val iso_3166_2: String? = null, // ISO 3166-2 code

    @SerializedName("state")
    val state: String? = null, // State or region

    @SerializedName("languagecodes")
    val languagecodes: String? = null, // Language codes

    @SerializedName("votes")
    val votes: Int? = null, // Number of votes

    @SerializedName("lastchangetime")
    val lastchangetime: String? = null, // Last change time

    @SerializedName("lastchangetime_iso8601")
    val lastchangetime_iso8601: String? = null, // Last change time in ISO8601

    @SerializedName("codec")
    val codec: String? = null, // Audio codec

    @SerializedName("bitrate")
    val bitrate: Int? = null, // Bitrate in kbps

    @SerializedName("hls")
    val hls: Int? = null, // HLS stream flag

    @SerializedName("lastcheckok")
    val lastcheckok: Int? = null, // Last check status

    @SerializedName("lastchecktime")
    val lastchecktime: String? = null, // Last check time

    @SerializedName("lastchecktime_iso8601")
    val lastchecktime_iso8601: String? = null, // Last check time in ISO8601

    @SerializedName("lastcheckoktime")
    val lastcheckoktime: String? = null, // Last successful check time

    @SerializedName("lastcheckoktime_iso8601")
    val lastcheckoktime_iso8601: String? = null, // Last successful check time in ISO8601

    @SerializedName("lastlocalchecktime")
    val lastlocalchecktime: String? = null, // Last local check time

    @SerializedName("lastlocalchecktime_iso8601")
    val lastlocalchecktime_iso8601: String? = null, // Last local check time in ISO8601

    @SerializedName("clicktimestamp")
    val clicktimestamp: String? = null, // Last click timestamp

    @SerializedName("clicktimestamp_iso8601")
    val clicktimestamp_iso8601: String? = null, // Last click timestamp in ISO8601

    @SerializedName("clickcount")
    val clickcount: Int? = null, // Number of clicks

    @SerializedName("clicktrend")
    val clicktrend: Int? = null, // Click trend

    @SerializedName("ssl_error")
    val ssl_error: Int? = null, // SSL error status

    @SerializedName("geo_lat")
    val geo_lat: Double? = null, // Latitude

    @SerializedName("geo_long")
    val geo_long: Double? = null, // Longitude

    @SerializedName("geo_distance")
    val geo_distance: Double? = null, // Distance from user

    @SerializedName("has_extended_info")
    val has_extended_info: Boolean? = null // Extended info flag
)