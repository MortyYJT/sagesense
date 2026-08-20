package com.mortyyjt.sagesense.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RiskEventEntity::class, WatchlistEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SageSenseDatabase : RoomDatabase() {
    abstract fun riskEventDao(): RiskEventDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile private var instance: SageSenseDatabase? = null

        fun get(context: Context): SageSenseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SageSenseDatabase::class.java,
                "sagesense.db",
            ).build().also { instance = it }
        }
    }
}
