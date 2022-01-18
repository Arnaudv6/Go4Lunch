package com.freemyip.arnaudv6.go4lunch.data.session

import android.net.Uri
import android.util.Log
import com.freemyip.arnaudv6.go4lunch.data.ConnectivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class SessionRepository @Inject constructor(
    connectivityRepository: ConnectivityRepository  // todo a repo must never depend on another repo : emerge a usecase
) {
    val sessionFlow: Flow<Session?> = connectivityRepository.isNetworkAvailableFlow.map {
        if (it) Session(1, "gmail") else null
    }

    companion object {
        const val URL = "https://arnaudv6.eu.auth0.com"
        const val CLIENT_ID = "IpKogHzhn6NoGh9aP6Xu8nYK5Pe25i60"
        const val REDIRECT_URI = "https://arnaudv6.freemyip.com"
    }

    @ExperimentalCoroutinesApi
    val authorizationRequestFlow: Flow<AuthorizationRequest> = callbackFlow {
        // or fetchFromUrl with [URL]/well-known/openid-configuration
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(URL),
            AuthorizationServiceConfiguration.RetrieveConfigurationCallback { serviceConfiguration, ex ->
                if (ex != null || serviceConfiguration == null) {
                    Log.d(this.javaClass.canonicalName, "failed to fetch configuration")
                    return@RetrieveConfigurationCallback
                    // todo Nino: proper way to retry?
                } else {
                    trySend(
                        AuthorizationRequest.Builder(
                            serviceConfiguration,
                            CLIENT_ID,
                            ResponseTypeValues.CODE,
                            Uri.parse(REDIRECT_URI)
                        ).build()
                    )
                }
            })
    }

}

