package com.cleanup.go4lunch.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.MainApplication
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val useCase: UseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {
    val navNumSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent<Int>()

    val viewStateFlow: Flow<MainViewState> = useCase.sessionUserFlow.map {
        if (it==null) MainViewState(
            null,
            appContext.getString(R.string.not_connected),
            appContext.getString(R.string.not_connected)
        )
        else MainViewState(
            it.user.avatarUrl,
            listOfNotNull(it.user.firstName ,it.user.lastName.uppercase()).joinToString(separator = " "),
            it.connectedThrough
        )
    }

    init {
        navNumSingleLiveEvent.value = useCase.getNavNum()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.updateUsers()
        }
    }

    fun onDestroy(num: Int) {
        useCase.setNavNum(num)
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
            useCase.cachedPOIsListFlow.first {
                val ids = it.map { poiEntity -> poiEntity.id }
                for (i: Int in 1..12) {
                    useCase.insertUser(
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
