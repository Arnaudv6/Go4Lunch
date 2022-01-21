package com.freemyip.arnaudv6.go4lunch.ui.main

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.ConnectivityRepository
import com.freemyip.arnaudv6.go4lunch.data.GpsProviderWrapper
import com.freemyip.arnaudv6.go4lunch.data.SearchRepository
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.session.SessionRepository
import com.freemyip.arnaudv6.go4lunch.data.settings.SettingsRepository
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import com.freemyip.arnaudv6.go4lunch.domain.useCase.GetSessionUserUseCase
import com.freemyip.arnaudv6.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val poiRepository: PoiRepository,
    private val sessionRepository: SessionRepository,
    connectivityRepository: ConnectivityRepository,
    settingsRepository: SettingsRepository,
    private val searchRepository: SearchRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val sessionUserUseCase: GetSessionUserUseCase,
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

    val viewStateFlow: LiveData<MainViewState> = sessionUserUseCase().map {
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

    fun setAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        if (response != null) {
            viewModelScope.launch {
                sessionRepository.setAuthState(response, exception)
            }
            // todo move this to after GetSessionUserUseCase
            viewActionSingleLiveEvent.postValue(MainViewAction.SnackBar("Login successful"))
        } else if (exception != null) {
            viewActionSingleLiveEvent.postValue(MainViewAction.SnackBar("Login failed"))
        }
    }

    fun onLogoutClicked() {
        viewActionSingleLiveEvent.postValue(
            MainViewAction.InitAuthorization(sessionRepository.authorizationRequest)
        )
        /*
        viewModelScope.launch {
            withTimeoutOrNull(7_000) {  // no onErrorAction here, as repo might be alive.
                sessionRepository.authorizationRequestFlow.filterNotNull()
                    .firstOrNull()?.let {
                        viewActionSingleLiveEvent.postValue(MainViewAction.InitAuthorization(it))
                    }
            }
        }
        */
        /*
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
        */
    }

    fun onLunchClicked() {
        suspendOrElse(onErrorAction = MainViewAction.SnackBar(application.getString(R.string.not_going_at_noon))) {
            sessionUserUseCase().filterNotNull()
                .firstOrNull()?.user?.goingAtNoon?.let {
                    viewActionSingleLiveEvent.postValue(MainViewAction.LaunchDetail(it))
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

    // withTimeoutOrNull() + onErrorAction
    private fun suspendOrElse(
        timeout: Long = 2_000,
        onErrorAction: MainViewAction,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            val job = launch(allDispatchers.ioDispatcher) { block() }
            delay(timeout)
            if (job.isActive) {
                job.cancel(application.getString(R.string.start_activity_timeout))
                viewActionSingleLiveEvent.postValue(onErrorAction)
            }
        }
    }
}


