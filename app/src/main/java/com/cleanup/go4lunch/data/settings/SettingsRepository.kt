package com.cleanup.go4lunch.data.settings

import android.util.Log
import com.cleanup.go4lunch.data.MyLocationUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getInitialBox(): BoundingBox = run {
        val box = settingsDao.getBox()
        if (box == null) MyLocationUtils.FRANCE_BOX
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

    suspend fun getNavNum(): Int? = settingsDao.getNavNum()?.data

    fun setNavNum(num: Int) {
        CoroutineScope(ioDispatcher).launch {
            settingsDao.setNavNum(IntEntity(IntEntity.NAV_NUM, num))
        }
    }

}