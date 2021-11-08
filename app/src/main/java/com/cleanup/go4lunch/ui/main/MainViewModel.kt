package com.cleanup.go4lunch.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.UpdateUserUseCase
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,  // todo move to usecase?
    private val useCase: UseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {
    val navNumSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent<Int>()

    val viewStateFlow: Flow<MainViewState> = useCase.sessionUserFlow.map {
        Log.e(this.javaClass.canonicalName, "sessionUser: $it")
        if (it == null) MainViewState(
            null,
            appContext.getString(R.string.not_connected),
            appContext.getString(R.string.not_connected)
        )
        else MainViewState(
            it.user.avatarUrl,
            listOfNotNull(
                it.user.firstName,
                it.user.lastName.uppercase()
            ).joinToString(separator = " "),
            it.connectedThrough
        )
    }

    init {
        navNumSingleLiveEvent.value = useCase.getNavNum()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            updateUserUseCase()
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

    fun onDisconnectClicked() {
        viewModelScope.launch(Dispatchers.IO) {

            // TODO ARNAUD USE IT
            var allGood = true

            useCase.cachedPOIsListFlow.first {
                val ids = it.map { poiEntity -> poiEntity.id }
                for (i: Int in 1..12) {
                    if (!useCase.insertUser(
                        User(
                            i.toLong(),
                            "Agatha$i",
                            "Christie$i",
                            "https://i.pravatar.cc/150?u=$i",
                            ids[i] // will crash if <12 POIs
                        )
                    )) {
                        allGood = false
                    }
                }
                true
            }
        }
    }
}
