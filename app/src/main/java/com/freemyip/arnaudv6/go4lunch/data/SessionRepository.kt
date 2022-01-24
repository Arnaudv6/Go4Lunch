package com.freemyip.arnaudv6.go4lunch.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.freemyip.arnaudv6.go4lunch.data.users.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.openid.appauth.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@Singleton
class SessionRepository @Inject constructor(
    @ApplicationContext appContext: Context
) {
    companion object {
        const val URL = "https://arnaudv6.eu.auth0.com"
        const val CLIENT_ID = "IpKogHzhn6NoGh9aP6Xu8nYK5Pe25i60"
        const val REDIRECT_URI =
            "com.freemyip.arnaudv6://arnaudv6.freemyip.com/go4lunch/oauth2redirect"
    }

    private val authService = AuthorizationService(appContext)

    // AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse(URL))
    //  is preferred, but implemented with crappy asynchronous behaviour
    //  https://github.com/openid/AppAuth-Android/issues/796
    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("$URL/authorize"),
        Uri.parse("$URL/oauth/token")
    )

    val authorizationRequest = AuthorizationRequest.Builder(
        serviceConfig,
        CLIENT_ID,
        ResponseTypeValues.CODE,
        // ResponseTypeValues.ID_TOKEN,
        Uri.parse(REDIRECT_URI)
    )
        .setScopes(
            AuthorizationRequest.Scope.OPENID,
            AuthorizationRequest.Scope.PROFILE,
            AuthorizationRequest.Scope.EMAIL,
        )
        // https://auth0.com/docs/secure/tokens/id-tokens/id-token-structure
        .build()

    private val userInfoMutableStateFlow = MutableStateFlow<User?>(null)
    val userInfoFlow: Flow<User?> = userInfoMutableStateFlow.asStateFlow()

    private var authState: AuthState? = null

    fun setAuthState(response: AuthorizationResponse, exception: AuthorizationException?) {
        authState = AuthState(response, exception)

        authService.performTokenRequest(response.createTokenExchangeRequest()) { resp, ex ->
            if (ex != null) {
                authState = AuthState()
            } else {
                resp?.let {
                    authState?.update(resp, ex)
                    it.idToken?.let { token ->
                        val truncated = token.split('.')[1]
                        val decoded64 = String(Base64.decode(truncated, Base64.DEFAULT))
                        val jsonObject = JSONObject(decoded64)
                        userInfoMutableStateFlow.tryEmit(
                            User(
                                email = jsonObject.getString("email"),
                                firstName = jsonObject.getString("given_name"),
                                lastName = jsonObject.getString("family_name"),
                                avatarUrl = jsonObject.getString("picture").replace("\\/", "/")
                            )
                        )
                    }
                }
            }
        }
    }
}

