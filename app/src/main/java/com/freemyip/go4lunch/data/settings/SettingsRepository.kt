package com.freemyip.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.freemyip.go4lunch.R
import com.freemyip.go4lunch.data.AllDispatchers
import com.freemyip.go4lunch.ui.utils.NotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val application: Application,
    private val settingsDao: SettingsDao,
    private val allDispatchers: AllDispatchers,
    private val workManager: WorkManager,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private val FRANCE_BOX = BoundingBox(51.404, 8.341, 42.190, -4.932)
        private const val WORKER_ID_NAME = "NOTIFICATION WORKER"
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

    suspend fun getInitialBox(): BoundingBox = settingsDao.getBox()
        ?.let { BoundingBox(it.north, it.east, it.south, it.west) } ?: FRANCE_BOX

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(allDispatchers.ioDispatcher).launch {
            settingsDao.setBox(boxEntity)
        }
    }

    fun setNotification(enable: Boolean) {
        Log.d(this.javaClass.canonicalName, "enableNotifications: $enable")

        if (enable) {
            val nextLunch =
                if (LocalDateTime.now().hour < 12) LocalDate.now().atTime(LocalTime.NOON)
                else LocalDate.now().plusDays(1).atTime(LocalTime.NOON)

            val seconds = LocalDateTime.now().until(nextLunch, ChronoUnit.SECONDS)

            val work = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(7, TimeUnit.SECONDS).build() // setInputData()

            workManager.beginUniqueWork(WORKER_ID_NAME, ExistingWorkPolicy.REPLACE, work).enqueue()

            // todo this worker is one shot, and cares not about weekends.
        } else {
            workManager.cancelUniqueWork(WORKER_ID_NAME)
        }
    }

    fun getTheme(): Int = themes[sharedPreferences.getString(
        application.getString(R.string.preferences_theme_key),
        application.getString(R.string.preferences_theme_key_system)
    )] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM  // until preferences-ktx fix

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


