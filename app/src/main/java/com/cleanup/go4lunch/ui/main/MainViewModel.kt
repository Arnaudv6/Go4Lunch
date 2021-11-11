package com.cleanup.go4lunch.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.useCase.UpdateUserUseCase
import com.cleanup.go4lunch.data.useCase.UseCase
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,  // todo move to usecase?
    private val settingsRepository: SettingsRepository,
    useCase: UseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    @ApplicationContext appContext: Context,
) : ViewModel() {
    val navNumSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent<Int>()

    val viewStateFlow: Flow<MainViewState> = useCase.sessionUserFlow.map {
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

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            updateUserUseCase()
        }
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
