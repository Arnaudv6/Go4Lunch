package com.cleanup.go4lunch.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsDao

@Database(entities = [BoxEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val settingsDao: SettingsDao
}
