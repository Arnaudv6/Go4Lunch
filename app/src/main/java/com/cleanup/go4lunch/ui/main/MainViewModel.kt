package com.cleanup.go4lunch.ui.main

import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    suspend fun getNavNum(): Int = settingsRepository.getNavNum() ?: 0

    fun onDestroy(num: Int) {
        settingsRepository.setNavNum(num)
    }

    fun onStop() {
        gpsProviderWrapper.stopWrapper()
    }

    fun onStart() {
        gpsProviderWrapper.startLocationProvider()
    }

    fun permissionsUpdated(fine: Boolean, coarse:Boolean) {
        gpsProviderWrapper.locationPermissionUpdate(fine, coarse)
    }
}
