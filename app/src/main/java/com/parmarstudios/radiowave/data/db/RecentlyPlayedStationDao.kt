package com.parmarstudios.radiowave.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing recently played radio stations in the local Room database.
 * Provides methods to insert, update, retrieve, and delete recently played stations.
 */
@Dao
interface RecentlyPlayedStationDao {

    /**
     * Inserts a [RecentlyPlayedStation] into the database.
     * If a station with the same ID exists, it will be replaced.
     *
     * @param station The [RecentlyPlayedStation] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayedStation(station: RecentlyPlayedStation)

    /**
     * Retrieves all recently played stations, ordered by start time (most recent first).
     *
     * @return A list of all [RecentlyPlayedStation] entries.
     */
    @Query("SELECT * FROM recently_played_station ORDER BY startTime DESC")
    suspend fun getAllRecentlyPlayedStations(): List<RecentlyPlayedStation>

    /**
     * Updates an existing [RecentlyPlayedStation] entry in the database.
     *
     * @param station The [RecentlyPlayedStation] to update.
     */
    @Update
    suspend fun updateRecentlyPlayedStation(station: RecentlyPlayedStation)

    /**
     * Deletes all recently played stations from the database.
     */
    @Query("DELETE FROM recently_played_station")
    suspend fun clearAll()

    /**
     * Retrieves all recently played stations as a Flow, ordered by start time (most recent first).
     * Useful for observing changes in real-time.
     *
     * @return A [Flow] emitting lists of [RecentlyPlayedStation] entries.
     */
    @Query("SELECT * FROM recently_played_station ORDER BY startTime DESC")
    fun getAllRecentlyPlayedStationsFlow(): Flow<List<RecentlyPlayedStation>>

    /**
     * Deletes a recently played station by its unique ID.
     *
     * @param id The unique identifier of the entry to delete.
     */
    @Query("DELETE FROM recently_played_station WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Retrieves the recent play history for a specific station, ordered by start time (most recent first).
     *
     * @param stationUuid The unique identifier of the station.
     * @return A list of [RecentlyPlayedStation] entries for the given station.
     */
    @Query("SELECT * FROM recently_played_station WHERE stationUuid = :stationUuid ORDER BY startTime DESC")
    suspend fun getRecentHistoryForStation(stationUuid: String): List<RecentlyPlayedStation>
}