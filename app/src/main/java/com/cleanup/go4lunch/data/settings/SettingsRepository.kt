package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
import com.cleanup.go4lunch.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import java.util.*
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

    val themes: Map<String, Int> = hashMapOf(
        Pair(
            application.getString(R.string.preferences_theme_key_dark),
            AppCompatDelegate.MODE_NIGHT_YES
        ),
        Pair(
            application.getString(R.string.preferences_theme_key_light),
            AppCompatDelegate.MODE_NIGHT_NO
        ),
        Pair(
            application.getString(R.string.preferences_theme_key_system),
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ),
    )

    val locales: Map<String, Locale> = hashMapOf(
        Pair(application.getString(R.string.preferences_locale_key_english), Locale.ENGLISH),
        Pair(application.getString(R.string.preferences_locale_key_french), Locale.FRENCH),
        Pair(application.getString(R.string.preferences_locale_key_system), Locale.getDefault()),
    )

    suspend fun getInitialBox(): BoundingBox = settingsDao.getBox()
        ?.let { BoundingBox(it.north, it.east, it.south, it.west) } ?: FRANCE_BOX

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(allDispatchers.ioDispatcher).launch {
            settingsDao.setBox(boxEntity)
        }
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun getNotificationEnabled(): Boolean = preferences.getBoolean(
        application.getString(R.string.preferences_notif_key),
        true
    )

    fun getTheme(): Int = themes[preferences.getString(
        application.getString(R.string.preferences_theme_key),
        application.getString(R.string.preferences_theme_key_system)
    )] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM  // until preferences-ktx fix

    fun getLocale(): Locale = locales[preferences.getString(
        application.getString(R.string.preferences_locale_key),
        application.getString(R.string.preferences_locale_key_system)
    )] ?: Locale.getDefault()  // until preferences-ktx fix

    /* no need for flows. onPreferenceChangeListener() in preferenceActivity for changes

    // Generic. Trade-off: one OnSharedPreferenceChangeListener() per watched value
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
    */
}


