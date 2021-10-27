package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.Context
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.MyLocationUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    application: Application,
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

    companion object {
        private const val NAV_NUM: String = "NAV_NUM"
    }

    private val preferences = application.getSharedPreferences(
        application.getString(R.string.preferences_file),
        Context.MODE_PRIVATE
    )

    fun getNavNum(): Int = preferences.getInt(NAV_NUM, 0)

    fun setNavNum(num: Int) = preferences.edit().putInt(NAV_NUM, num).apply()
}
