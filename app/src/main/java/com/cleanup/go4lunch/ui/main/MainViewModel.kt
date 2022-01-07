package com.cleanup.go4lunch.ui.main

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
import com.cleanup.go4lunch.data.ConnectivityRepository
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.SearchRepository
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val poiRepository: PoiRepository,
    connectivityRepository: ConnectivityRepository,
    settingsRepository: SettingsRepository,
    private val searchRepository: SearchRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val sessionUserUseCase: SessionUserUseCase,
    private val application: Application,
    private val allDispatchers: AllDispatchers,
) : ViewModel() {

    val viewActionSingleLiveEvent: SingleLiveEvent<MainViewAction> = SingleLiveEvent()

    init {
        AppCompatDelegate.setDefaultNightMode(settingsRepository.getTheme())

        // to collect from MainApp with relevant lifecycle, we'd have to track (started) activities
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            connectivityRepository.isNetworkAvailableFlow.collect {
                if (it) usersRepository.updateMatesList()
            }
        }
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            usersRepository.matesListFlow.collect {
                val num = poiRepository.fetchPOIsInList(
                    ids = it.mapNotNull { user -> user.goingAtNoon },
                    refreshExisting = false
                )
                if (num != 0) viewActionSingleLiveEvent.postValue(
                    MainViewAction.SnackBar(
                        application.resources.getQuantityString(
                            R.plurals.received_mates_pois,
                            num,
                            num
                        )
                    )
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
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            usersRepository.insertUser(User(1, "Arnaud", "v6", "https://avatars.githubusercontent.com/u/6125315",1181634478))

            usersRepository.insertUser(User(11, "Caroline", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704d",1181634478))
            usersRepository.insertUser(User(12, "Jack", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704e",1181635071))
            usersRepository.insertUser(User(13, "Chloe", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704f",1223768234))
            usersRepository.insertUser(User(14, "Vincent", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704a",1223805591))
            usersRepository.insertUser(User(15, "Elodie", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704b",1224673311))
            usersRepository.insertUser(User(16, "Sylvain", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026704c",1473397603))
            usersRepository.insertUser(User(17, "Laetitia", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026703d",1746634556))
            usersRepository.insertUser(User(18, "Dan", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026703b",2363182980))
            usersRepository.insertUser(User(19, "Joseph", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29a26704e",2450486148))
            usersRepository.insertUser(User(20, "Emma", "Dupont", "https://i.pravatar.cc/150?u=a042581f4e29026706d",2556945898))

            usersRepository.insertUser(User(21, "Mariah", "Dupont", "https://i.pravatar.cc/150?u=a042123f4e654867123",null))
        }
    }

    fun onLunchClicked() {
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            val job = launch {  // filterNotNull().firstOrNull(): OK
                sessionUserUseCase.sessionUserFlow.filterNotNull()
                    .firstOrNull()?.user?.goingAtNoon?.let {
                        viewActionSingleLiveEvent.postValue(MainViewAction.LaunchDetail(it))
                    }
            }
            delay(2_000) // there may be other places where a timeout is relevant.
            if (job.isActive) {
                job.cancel("Do not start activity as we get connection over 2 secs later")
                viewActionSingleLiveEvent.postValue(
                    MainViewAction.SnackBar(application.getString(R.string.not_going_at_noon))
                )
            }
        }
    }

    fun searchSubmit(query: String?, searchMates: Boolean) {
        if (!searchMates && !query.isNullOrEmpty()) {
            viewModelScope.launch(allDispatchers.ioDispatcher) {
                poiRepository.fetchPOIsByName(query).let {
                    viewActionSingleLiveEvent.postValue(
                        MainViewAction.SnackBar(
                            application.resources.getQuantityString(
                                R.plurals.received_pois,
                                it,
                                it
                            )
                        )
                    )
                }
            }
        }
    }

    fun searchTermChange(newText: String?) = searchRepository.setSearchTerms(newText)

}


