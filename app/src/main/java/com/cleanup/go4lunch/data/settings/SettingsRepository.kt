package com.cleanup.go4lunch.data.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {

    val boxFlow: Flow<BoundingBox> = settingsDao.getBox().mapNotNull {
        if (it == null) it
        else
            BoundingBox(
                it.north,
                it.east,
                it.south,
                it.west
            )
    }

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            settingsDao.setBox(boxEntity)
        }
    }

}