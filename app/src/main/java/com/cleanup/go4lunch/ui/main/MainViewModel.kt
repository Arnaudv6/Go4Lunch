package com.cleanup.go4lunch.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val navNumLiveData: LiveData<Int?> = settingsRepository.navNumFlow.asLiveData()

    fun onDestroy(num: Int) {
        settingsRepository.setNavNum(num)
    }

    fun onStop() {
        gpsProviderWrapper.stopWrapper()
    }

    fun onStart() {
        gpsProviderWrapper.startLocationProvider()
    }
}
