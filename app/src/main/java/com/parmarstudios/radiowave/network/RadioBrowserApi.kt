package com.parmarstudios.radiowave.network

import com.parmarstudios.radiowave.data.RadioStation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Data class representing a request to search for radio stations.
 *
 * @property country The country to filter stations by.
 * @property name The name (or partial name) of the station to search for.
 * @property limit The maximum number of results to return.
 * @property order The field to order results by (e.g., "name", "votes").
 * @property reverse Whether to reverse the order of results.
 * @property offset The offset for pagination (default is 0).
 */
data class StationSearchRequest(
    val country: String,   // Country filter
    val name: String,      // Station name filter
    val limit: Int,        // Max results to return
    val order: String,     // Order by field
    val reverse: Boolean,  // Reverse order flag
    val offset: Int = 0    // Pagination offset (default 0)
)

/**
 * Retrofit API interface for interacting with the RadioBrowser backend.
 * Provides methods to search for radio stations.
 */
interface RadioBrowserApi {
    /**
     * Searches for radio stations matching the given criteria.
     *
     * @param request The [StationSearchRequest] containing search parameters.
     * @return A list of [RadioStation] objects matching the search.
     */
    @POST("json/stations/search")
    suspend fun searchStations(
        @Body request: StationSearchRequest // Search parameters in request body
    ): List<RadioStation>

    @GET("json/stations/byuuid/{uuid}")
    suspend fun getStationByUuid(@Path("uuid") uuid: String): List<RadioStation>
}