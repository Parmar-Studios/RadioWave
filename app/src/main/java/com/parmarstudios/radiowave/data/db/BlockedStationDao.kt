package com.parmarstudios.radiowave.data.db

import androidx.room.*

/**
 * Data Access Object (DAO) for managing blocked radio stations in the local Room database.
 * Provides methods to add, remove, check, retrieve, and clear blocked stations.
 */
@Dao
interface BlockedStationDao {

    /**
     * Inserts a [BlockedStation] into the database.
     * If a station with the same UUID exists, it will be replaced.
     *
     * @param station The [BlockedStation] to add.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBlocked(station: BlockedStation)

    /**
     * Removes a [BlockedStation] from the database.
     *
     * @param station The [BlockedStation] to remove.
     */
    @Delete
    suspend fun removeBlocked(station: BlockedStation)

    /**
     * Checks if a station with the given UUID is blocked.
     *
     * @param uuid The unique identifier of the station.
     * @return `true` if the station is blocked, `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_stations WHERE stationUuid = :uuid)")
    suspend fun isBlocked(uuid: String): Boolean

    /**
     * Retrieves all blocked stations from the database.
     *
     * @return A list of all [BlockedStation] entries.
     */
    @Query("SELECT * FROM blocked_stations")
    suspend fun getAllBlocked(): List<BlockedStation>

    /**
     * Deletes all blocked stations from the database.
     */
    @Query("DELETE FROM blocked_stations")
    suspend fun clearAll()
}