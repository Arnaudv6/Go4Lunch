package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.MyLocationUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    application: Application,
    private val settingsDao: SettingsDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private const val NAV_NUM: String = "NAV_NUM"
    }

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

    private val preferences = application.getSharedPreferences(
        application.getString(R.string.preferences_file),
        Context.MODE_PRIVATE
    )

    @WorkerThread
    fun getNavNum(): Int = preferences.getInt(NAV_NUM, 0)

    @WorkerThread
    fun setNavNum(num: Int) = preferences.edit().putInt(NAV_NUM, num).apply()

    fun getConnectionType(): String = "gmail"

    // todo make this code relevant
    private val idMutableStateFlow = MutableStateFlow<Long?>(1) // todo replace 1 with null

    val idStateFlow: Flow<Long?> = idMutableStateFlow.asStateFlow()
}
