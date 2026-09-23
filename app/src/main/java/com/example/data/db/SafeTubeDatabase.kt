package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ParentSettingsEntity::class,
        UserProfileEntity::class,
        WatchActivityEntity::class,
        OfflineVideoEntity::class,
        SearchQueryEntity::class,
        WatchHistoryEntity::class,
        WatchLaterEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SafeTubeDatabase : RoomDatabase() {
    abstract fun safeTubeDao(): SafeTubeDao
    abstract fun offlineVideoDao(): OfflineVideoDao

    companion object {
        @Volatile
        private var INSTANCE: SafeTubeDatabase? = null

        fun getInstance(context: Context): SafeTubeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeTubeDatabase::class.java,
                    "safetube_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
