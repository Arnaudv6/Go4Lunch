package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cleanup.go4lunch.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    @ExperimentalCoroutinesApi
    val notificationsEnabledFlow: Flow<Boolean> = callbackFlow {
        trySend(initialValue)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key.equals(notificationKey)) {
                trySend(preferences.getBoolean(notificationKey, true))
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}


