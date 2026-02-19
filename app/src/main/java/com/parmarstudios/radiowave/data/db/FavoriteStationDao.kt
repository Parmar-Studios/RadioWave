package com.parmarstudios.radiowave.data.db

import androidx.room.*

/**
 * Data Access Object (DAO) for managing favorite radio stations in the local Room database.
 * Provides methods to add, remove, check, retrieve, and clear favorite stations.
 */
@Dao
interface FavoriteStationDao {

    /**
     * Inserts a [FavoriteStation] into the database.
     * If a station with the same UUID exists, it will be replaced.
     *
     * @param station The [FavoriteStation] to add.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(station: FavoriteStation)

    /**
     * Removes a [FavoriteStation] from the database.
     *
     * @param station The [FavoriteStation] to remove.
     */
    @Delete
    suspend fun removeFavorite(station: FavoriteStation)

    /**
     * Checks if a station with the given UUID is marked as favorite.
     *
     * @param uuid The unique identifier of the station.
     * @return `true` if the station is a favorite, `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationUuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean

    /**
     * Retrieves all favorite stations from the database.
     *
     * @return A list of all [FavoriteStation] entries.
     */
    @Query("SELECT * FROM favorite_stations")
    suspend fun getAllFavorites(): List<FavoriteStation>

    /**
     * Deletes all favorite stations from the database.
     */
    @Query("DELETE FROM favorite_stations")
    suspend fun clearAll()
}