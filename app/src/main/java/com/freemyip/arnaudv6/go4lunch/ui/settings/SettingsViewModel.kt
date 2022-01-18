package com.freemyip.arnaudv6.go4lunch.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.settings.SettingsRepository
import com.freemyip.arnaudv6.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val poiRepository: PoiRepository,
    private val application: Application,
    private val allDispatchers: AllDispatchers,
) : ViewModel() {

    val snackBarSingleLiveEvent: SingleLiveEvent<String> = SingleLiveEvent()

    fun themeSet(theme: Any) {
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

    fun enableNotifications(enable: Boolean) = settingsRepository.setNotification(enable)

}

