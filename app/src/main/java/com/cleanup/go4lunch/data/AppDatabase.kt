package com.cleanup.go4lunch.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cleanup.go4lunch.data.pois.PoiDao
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsDao

@Database(
    entities = [BoxEntity::class, PoiEntity::class],
    version = 1,
    exportSchema = false // to get red of a compile-time warning
)
abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val poiDao: PoiDao
}
