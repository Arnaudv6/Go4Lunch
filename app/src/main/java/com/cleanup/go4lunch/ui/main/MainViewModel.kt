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
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    connectivityRepository: ConnectivityRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val settingsRepository: SettingsRepository,
    private val sessionUserUseCase: SessionUserUseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {

    val viewActionSingleLiveEvent: SingleLiveEvent<MainViewAction> = SingleLiveEvent()

    init {
        viewActionSingleLiveEvent.value = MainViewAction.StartNavNum(settingsRepository.getNavNum())

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
            // Todo Nino filterNotNull().firstOrNull(): OK?
            sessionUserUseCase.sessionUserFlow.filterNotNull().firstOrNull().let {
                val id = it?.user?.goingAtNoon
                if (id != null) viewActionSingleLiveEvent.value = MainViewAction.LaunchDetail(id)
            }
        }
    }
}
