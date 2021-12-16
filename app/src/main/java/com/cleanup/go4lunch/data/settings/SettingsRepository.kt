package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
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
    private val application: Application,
    private val settingsDao: SettingsDao,
    private val allDispatchers: AllDispatchers,
) {

    companion object {
        private val FRANCE_BOX = BoundingBox(51.404, 8.341, 42.190, -4.932)
    }

    suspend fun getInitialBox(): BoundingBox = settingsDao.getBox()
        ?.let { BoundingBox(it.north, it.east, it.south, it.west) } ?: FRANCE_BOX

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(allDispatchers.ioDispatcher).launch {
            settingsDao.setBox(boxEntity)
        }
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val preferencesKeyNotifications = application.getString(R.string.preferences_notif_key)
    private val preferencesKeyTheme = application.getString(R.string.preferences_theme_key)
    private val preferencesKeyLocale = application.getString(R.string.preferences_locale_key)


    // todo Nino : monitoring changes from here and pushing them back up to settingsVM: OK?
    @ExperimentalCoroutinesApi
    val notificationsEnabledFlow: Flow<Any> = callbackFlow {
        // trySend(getNotificationEnabled())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                preferencesKeyNotifications -> trySend(getNotificationEnabled())
                preferencesKeyTheme -> trySend(getTheme())
                preferencesKeyLocale -> trySend(getLocale())
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }


    fun getNotificationEnabled(): Boolean =
        preferences.getBoolean(preferencesKeyNotifications, true)

    fun getTheme(): String = preferences.getString(
        preferencesKeyTheme,
        application.getString(R.string.preferences_theme_key_system)
    ).orEmpty() // todo Nino : or double Bang

    fun getLocale(): String = preferences.getString(
        preferencesKeyLocale,
        application.getString(R.string.preferences_locale_key_system)
    ).orEmpty() // todo Nino : or double Bang

}


