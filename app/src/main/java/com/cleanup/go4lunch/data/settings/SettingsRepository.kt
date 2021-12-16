package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
import com.cleanup.go4lunch.exhaustive
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

    // nice generic way. The trade off is one OnSharedPreferenceChangeListener() per watched value
    @ExperimentalCoroutinesApi
    private inline fun <reified T> getNewValuesAsFlow(askedKey: String): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == askedKey) {
                trySend(
                    when (T::class) {
                        // seemingly useless default values. But preference just changed: it is set.
                        Boolean::class -> prefs.getBoolean(askedKey, true) as T
                        String::class -> prefs.getString(askedKey, String()) as T
                        else -> throw UnsupportedOperationException("no code for type ${T::class}")
                    }.exhaustive
                )
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val notifsEnabledChangeFlow = getNewValuesAsFlow<Boolean>(preferencesKeyNotifications)
    val themeChangeFlow = getNewValuesAsFlow<String>(preferencesKeyTheme)
    val localeChangeFlow = getNewValuesAsFlow<String>(preferencesKeyLocale)

    fun getNotificationEnabled(): Boolean =
        preferences.getBoolean(preferencesKeyNotifications, true)

    fun getTheme(): String = preferences.getString(
        preferencesKeyTheme,
        application.getString(R.string.preferences_theme_key_system)
    ).orEmpty() // until preferences-ktx allows for dropping orEmpty()

    fun getLocale(): String = preferences.getString(
        preferencesKeyLocale,
        application.getString(R.string.preferences_locale_key_system)
    ).orEmpty() // until preferences-ktx allows for dropping orEmpty()

}


