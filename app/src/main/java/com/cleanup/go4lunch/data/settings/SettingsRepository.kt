package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.cleanup.go4lunch.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    application: Application,
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

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val notificationKey = application.getString(R.string.preferences_notif_key)
    private val initialValue = preferences.getBoolean(notificationKey, true)

    private val notificationsEnabledMutableStateFlow = MutableStateFlow(initialValue)
    val notificationsEnabledFlow: Flow<Boolean> = notificationsEnabledMutableStateFlow.asStateFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key.equals(notificationKey)) {
                val enabled = preferences.getBoolean(notificationKey, true)
                notificationsEnabledMutableStateFlow.tryEmit(enabled)
            }
        }
    }

}


