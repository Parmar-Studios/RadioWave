package com.parmarstudios.radiowave.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main Room database for the RadioWave app.
 *
 * @property favoriteStationDao Data access object for favorite stations.
 * @property blockedStationDao Data access object for blocked stations.
 * @property recentlyPlayedStationDao Data access object for recently played stations.
 * @property serverDao Data access object for server information.
 */
@Database(
    entities = [FavoriteStation::class, BlockedStation::class, RecentlyPlayedStation::class, Server::class],
    version = 10
)
abstract class AppDatabase : RoomDatabase() {

    /** Returns the DAO for favorite stations. */
    abstract fun favoriteStationDao(): FavoriteStationDao

    /** Returns the DAO for blocked stations. */
    abstract fun blockedStationDao(): BlockedStationDao

    /** Returns the DAO for recently played stations. */
    abstract fun recentlyPlayedStationDao(): RecentlyPlayedStationDao

    /** Returns the DAO for server information. */
    abstract fun serverDao(): ServerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of [AppDatabase].
         * If the instance does not exist, it creates a new one.
         *
         * @param context The application context.
         * @return The singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            // Double-checked locking to ensure thread safety
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "radio-db" // Database file name
                )
                    .fallbackToDestructiveMigration() // Wipes and rebuilds instead of migrating if no migration object.
                    .build().also { INSTANCE = it }
            }
        }
    }
}