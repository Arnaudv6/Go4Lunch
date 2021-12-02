package com.cleanup.go4lunch.data.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private val FRANCE_BOX = BoundingBox(51.404, 8.341, 42.190, -4.932)
    }

    suspend fun getInitialBox(): BoundingBox = run {
        val box = settingsDao.getBox()
        if (box == null) FRANCE_BOX
        else BoundingBox(
            box.north,
            box.east,
            box.south,
            box.west
        )
    }

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(ioDispatcher).launch {
            settingsDao.setBox(boxEntity)
        }
    }
}


