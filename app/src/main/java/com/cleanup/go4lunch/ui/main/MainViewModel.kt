package com.cleanup.go4lunch.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.ConnectivityRepository
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    poiRepository: PoiRepository,
    connectivityRepository: ConnectivityRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val sessionUserUseCase: SessionUserUseCase,
    private val application: Application,
) : ViewModel() {

    val viewActionSingleLiveEvent: SingleLiveEvent<MainViewAction> = SingleLiveEvent()

    init {
        // to collect from MainApp with relevant lifecycle, we'd have to track (started) activities
        viewModelScope.launch {
            connectivityRepository.isNetworkAvailableFlow.collect {
                if (it) usersRepository.updateMatesList()
            }
        }
        viewModelScope.launch {
            usersRepository.matesListFlow.collect {
                // todo snackBar
                poiRepository.fetchPOIsInList(
                    ids = it.mapNotNull { user -> user.goingAtNoon },
                    refreshExisting = false
                )
            }
        }
    }

    val viewStateFlow: LiveData<MainViewState> = sessionUserUseCase.sessionUserFlow.map {
        Log.d(this.javaClass.canonicalName, "sessionUser: $it")
        if (it == null) MainViewState(
            null,
            application.getString(R.string.not_connected),
            application.getString(R.string.not_connected),
            null
        )
        else MainViewState(
            it.user.avatarUrl,
            listOfNotNull(
                it.user.firstName,
                it.user.lastName.uppercase()
            ).joinToString(separator = " "),
            it.connectedThrough,
            it.user.goingAtNoon
        )
    }.asLiveData()

    fun onDestroy() {
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

    fun onLogoutClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.insertUser(
                User(
                    13,
                    "Natasha",
                    "Zobovich",
                    null,
                    null
                )
            )
        }
    }

    fun onLunchClicked() {
        viewModelScope.launch(Dispatchers.Main) {
            val job = launch {  // filterNotNull().firstOrNull(): OK
                sessionUserUseCase.sessionUserFlow.filterNotNull()
                    .firstOrNull()?.user?.goingAtNoon?.let {
                        viewActionSingleLiveEvent.value = MainViewAction.LaunchDetail(it)
                    }
            }
            delay(2_000) // there may be other places where a timeout is relevant.
            if (job.isActive) {
                job.cancel("Do not start activity as we get connection over 2 secs later")
                viewActionSingleLiveEvent.value =
                    MainViewAction.SnackBar(application.getString(R.string.not_going_at_noon))
            }
        }
    }
}
