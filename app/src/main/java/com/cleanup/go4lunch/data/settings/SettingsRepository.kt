package com.cleanup.go4lunch.data.settings

import androidx.annotation.WorkerThread
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {

    @WorkerThread
    suspend fun getMapBox(): BoundingBox? {
        val box = settingsDao.getBox() ?: return null
        return BoundingBox(
           box.north,
           box.east,
           box.south,
           box.west
        )
    }

    @WorkerThread
    suspend fun setMapBox(boxEntity: BoxEntity) {
        settingsDao.setBox(boxEntity)
    }

}