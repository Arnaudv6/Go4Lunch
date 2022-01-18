package com.freemyip.arnaudv6.go4lunch.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiDao
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiEntity
import com.freemyip.arnaudv6.go4lunch.data.settings.BoxEntity
import com.freemyip.arnaudv6.go4lunch.data.settings.SettingsDao

@Database(
    entities = [
        BoxEntity::class,
        PoiEntity::class
    ],
    version = 1,
    exportSchema = false // get rid of a compile-time warning
)
abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
    abstract val poiDao: PoiDao
}
