package com.parmarstudios.radiowave.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.parmarstudios.radiowave.data.db.Server

/**
 * Data Access Object (DAO) for managing radio servers in the local Room database.
 * Provides methods to query, insert, and update server health and score.
 */
@Dao
interface ServerDao {

    /**
     * Retrieves all healthy servers, ordered by their score in descending order.
     *
     * @return A list of healthy [Server] entries.
     */
    @Query("SELECT * FROM servers WHERE isHealthy = 1 ORDER BY score DESC")
    suspend fun getHealthyServers(): List<Server>

    /**
     * Inserts a [Server] into the database.
     * If a server with the same URL exists, the insert is ignored.
     *
     * @param server The [Server] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(server: Server)

    /**
     * Updates the health status of a server and increments its score by 1.
     *
     * @param url The unique URL of the server to update.
     * @param isHealthy The new health status to set.
     */
    @Query("UPDATE servers SET isHealthy = :isHealthy, score = score + 1 WHERE url = :url")
    suspend fun updateHealthAndIncrementScore(url: String, isHealthy: Boolean)

    /**
     * Updates the health status of a server and decrements its score by 1 (not going below 0).
     *
     * @param url The unique URL of the server to update.
     * @param isHealthy The new health status to set.
     */
    @Query("UPDATE servers SET isHealthy = :isHealthy, score = MAX(score - 1, 0) WHERE url = :url")
    suspend fun updateHealthAndDecrementScore(url: String, isHealthy: Boolean)
}