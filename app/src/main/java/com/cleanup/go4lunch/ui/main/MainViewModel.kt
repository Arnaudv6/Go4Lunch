package com.cleanup.go4lunch.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.ConnectivityRepository
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    connectivityRepository: ConnectivityRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val sessionUserUseCase: SessionUserUseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {

    val viewActionSingleLiveEvent: SingleLiveEvent<MainViewAction> = SingleLiveEvent()

    init {
        // todo must move to APP when counting activities
        viewModelScope.launch {
            connectivityRepository.isNetworkAvailableFlow.collect {
                if (it) usersRepository.updateMatesList()
            }
        }
    }

    val viewStateFlow: LiveData<MainViewState> = sessionUserUseCase.sessionUserFlow.map {
        Log.e(this.javaClass.canonicalName, "sessionUser: $it")
        if (it == null) MainViewState(
            null,
            appContext.getString(R.string.not_connected),
            appContext.getString(R.string.not_connected),
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
            val job = launch {
                // Todo Nino filterNotNull().firstOrNull(): OK?
                sessionUserUseCase.sessionUserFlow.filterNotNull()
                    .firstOrNull()?.user?.goingAtNoon?.let {
                        viewActionSingleLiveEvent.value = MainViewAction.LaunchDetail(it)
                    }
            }
            delay(2_000) // todo Nino implement this in repository  for most requests
            if (job.isActive) {
                job.cancel("Do not start activity as we get connection over 2 secs later")
            }
        }
    }

    fun onLunchClicked2() {
        viewModelScope.launch(Dispatchers.Main) {
            val launchDetailAction: MainViewAction.LaunchDetail? = withTimeoutOrNull(2_000) {
                // Todo Nino filterNotNull().firstOrNull(): OK?
                sessionUserUseCase.sessionUserFlow
                    .filterNotNull()
                    .firstOrNull()?.let { sessionUser ->
                        sessionUser.user.goingAtNoon?.let {
                            MainViewAction.LaunchDetail(it)
                        }
                    }
            }

            if (launchDetailAction != null) {
                viewActionSingleLiveEvent.value = launchDetailAction
            } else {

            }
        }
    }

    sealed class Wrapper {
        data class Success(val data : SessionUser?) : Wrapper()
        object Timeout : Wrapper()
    }
}
