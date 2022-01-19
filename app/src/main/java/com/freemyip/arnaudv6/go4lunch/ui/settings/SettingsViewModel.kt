package com.freemyip.arnaudv6.go4lunch.ui.settings

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.settings.SettingsRepository
import com.freemyip.arnaudv6.go4lunch.ui.utils.NotificationWorker
import com.freemyip.arnaudv6.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val poiRepository: PoiRepository,
    private val application: Application,
    private val workManager: WorkManager,
    private val clock: Clock,
    private val allDispatchers: AllDispatchers,
) : ViewModel() {

    companion object {
        private const val WORKER_ID_NAME = "NOTIFICATION WORKER"
    }

    val snackBarSingleLiveEvent: SingleLiveEvent<String> = SingleLiveEvent()

    fun themeSet(theme: String) {
        AppCompatDelegate.setDefaultNightMode(
            settingsRepository.themes.getOrDefault(
                theme,  // settingsRepository.getTheme() takes some time to reflect new value
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )
    }

    fun clearCache() {
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            poiRepository.clearCache()
            snackBarSingleLiveEvent.postValue(application.getString(R.string.cache_cleared))
        }
    }

    fun enableNotifications(enable: Boolean) {
        Log.d(this::class.java.canonicalName, "enableNotifications: $enable")
        if (enable) {
            val nextLunch =
                if (LocalDateTime.now(clock).hour < 12) LocalDate.now(clock).atTime(LocalTime.NOON)
                else LocalDate.now(clock).plusDays(1).atTime(LocalTime.NOON)

            val seconds = LocalDateTime.now(clock).until(nextLunch, ChronoUnit.SECONDS)

            // todo : repeat this the next (working-)day
            //  use PeriodicWorkRequestBuilder<NotificationWorker>() see Daily Jobs here:
            //  https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
            //  or call this from NotificationWorker. race condition?
            // builder.setInputData(). Also replace 'seconds' with '15', to test.
            val work = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(seconds, TimeUnit.SECONDS).build()

            workManager.beginUniqueWork(
                WORKER_ID_NAME,
                ExistingWorkPolicy.REPLACE,
                work
            ).enqueue()
        } else {
            workManager.cancelUniqueWork(WORKER_ID_NAME)
            NotificationManagerCompat.from(application).cancelAll()
        }
    }
}

