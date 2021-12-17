package com.cleanup.go4lunch.ui.settings

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.ui.alarm.AlarmActivity
import com.cleanup.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val poiRepository: PoiRepository,
    private val application: Application,
    private val allDispatchers: AllDispatchers,
) : ViewModel() {

    val snackBarSingleLiveEvent: SingleLiveEvent<String> = SingleLiveEvent()

    companion object {
        private const val REQUEST_CODE = 4444
    }

    // todo enable notifications by default : observe from MainActivity? if not, 2 events => ViewAction

    // todo Arnaud -> worker route
    private val intent = Intent().setClass(application, AlarmActivity::class.java)

    @SuppressLint("UnspecifiedImmutableFlag")  // API 24+
    private val pendingIntent = PendingIntent.getActivity(
        application, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT
    )

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun themeSet(theme: Any) {
        AppCompatDelegate.setDefaultNightMode(
            settingsRepository.themes.getOrDefault(
                theme,  // settingsRepository.getTheme() takes some time to reflect new value
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )
    }

    // todo Nino : not sure we actually want can set app locale ? seems VM-wide setting ?
    fun themeLocale(locale: Any?) {
        application.applicationContext.resources.configuration.setLocale(
            // settingsRepository.getLocale() takes some time to reflect new value
            settingsRepository.locales.getOrDefault(locale, Locale.getDefault())
        )
    }

    fun clearCache() {
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            poiRepository.clearCache()
            snackBarSingleLiveEvent.postValue(application.getString(R.string.cache_cleared))
        }
    }

    fun enableNotifications(enable: Boolean) {
        Log.d(this.javaClass.canonicalName, "enableNotifications: $enable")
        if (enable) {
            val nextLunch =
                if (LocalDateTime.now().hour < 12) LocalDate.now().atTime(LocalTime.NOON)
                else LocalDate.now().plusDays(1).atTime(LocalTime.NOON)

            val nextLunch2 = LocalDateTime.now().plusSeconds(15)

/*
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
//                nextLunch2.toInstant(ZoneOffset.UTC).toEpochMilli(),
                15*1000,  // c'est lui qui marche
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
*/

        } else {
            alarmManager.cancel(pendingIntent)
        }
    }
}

