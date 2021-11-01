package com.cleanup.go4lunch.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val settingsRepository: SettingsRepository,
    private val usersRepository: UsersRepository,
    private val poiRepository: PoiRepository,
) : ViewModel() {
    val navNumLivedata: SingleLiveEvent<Int> = SingleLiveEvent<Int>()

    init {
        navNumLivedata.value = settingsRepository.getNavNum()
    }

    fun onDestroy(num: Int) {
        settingsRepository.setNavNum(num)
        gpsProviderWrapper.destroyWrapper()
    }

    fun onStop() {
        gpsProviderWrapper.stopWrapper()
    }

    fun onStart() {
        gpsProviderWrapper.startLocationProvider()
    }

    fun permissionsUpdated(fine: Boolean, coarse: Boolean) {
        gpsProviderWrapper.locationPermissionUpdate(fine, coarse)
    }

    @ExperimentalCoroutinesApi
    fun onDisconnectClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            poiRepository.cachedPOIsListFlow.first {
                val ids = it.map { poiEntity -> poiEntity.id }
                for (i: Int in 1..12) {
                    usersRepository.insertUser(
                        User(
                            i.toLong(),
                            "Agatha$i",
                            "Christie$i",
                            "https://i.pravatar.cc/150?u=$i",
                            ids[i] // will crash if <12 POIs
                        )
                    )
                }
                true
            }
        }
    }
}
