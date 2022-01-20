package com.freemyip.arnaudv6.go4lunch.data.session

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import net.openid.appauth.*
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class SessionRepository @Inject constructor() {
    companion object {
        const val URL = "https://arnaudv6.eu.auth0.com"
        const val CLIENT_ID = "IpKogHzhn6NoGh9aP6Xu8nYK5Pe25i60"
        const val REDIRECT_URI =
            "com.freemyip.arnaudv6://arnaudv6.freemyip.com/go4lunch/oauth2redirect"
    }

    private val authStateMutableStateFlow = MutableStateFlow<AuthState?>(null)
    val authStateFlow: Flow<AuthState?> = authStateMutableStateFlow.asStateFlow()

    fun setAuthState(response: AuthorizationResponse, exception: AuthorizationException?) {
        val authState = AuthState(response, exception)
        Log.e("TAG", "invoke: ${authState.idToken}", )
        authStateMutableStateFlow.value = authState
    }

    var userInfoEndPoint: Uri? = null

    private var notFinished: Boolean = true

    @ExperimentalCoroutinesApi
    val authorizationRequestFlow: Flow<AuthorizationRequest> = callbackFlow {
        // or fetchFromUrl with [URL]/well-known/openid-configuration
        while (notFinished) {
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(URL)) { conf, ex ->
                if (ex != null || conf == null) {
                    Log.d(this::class.java.canonicalName, "failed to fetch configuration")
                } else {
                    Log.d(
                        this::class.java.canonicalName,
                        "Retrieved endpoint: ${conf.authorizationEndpoint}"
                    )
                    trySend(
                        AuthorizationRequest.Builder(
                            conf,
                            CLIENT_ID,
                            ResponseTypeValues.CODE,
                            Uri.parse(REDIRECT_URI)
                        ).build()
                    )
                    userInfoEndPoint = conf.discoveryDoc?.userinfoEndpoint
                    notFinished = false
                }
            }
            delay(10_000)  // this could be exponential delay: 3, 5, 10, 15, 30, 60...
        }
        awaitClose { /* https://github.com/openid/AppAuth-Android/issues/796 */ }
    }
    /* if using a callbackFlow, https://github.com/openid/AppAuth-Android/issues/796 */
}

