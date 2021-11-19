package com.cleanup.go4lunch.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.ConnectivityRepository
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    connectivityRepository: ConnectivityRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,  // todo move to usecase?
    private val settingsRepository: SettingsRepository,
    sessionUserUseCase: SessionUserUseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {
    val connectivityFlow = connectivityRepository.isNetworkAvailableFlow.map {
        if (it) usersRepository.updateMatesList()
    } // todo Nino : je ne vois pas comment faire mieux que créer ça là et l'observer depuis l'activity

    val navNumSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent<Int>()

    val viewStateFlow: Flow<MainViewState> = sessionUserUseCase.sessionUserFlow.map {
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
    }

    init {
        navNumSingleLiveEvent.value = settingsRepository.getNavNum()
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

    fun onDisconnectClicked() = Unit
}
