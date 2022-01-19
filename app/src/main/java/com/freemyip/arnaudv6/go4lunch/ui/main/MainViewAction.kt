package com.freemyip.arnaudv6.go4lunch.ui.main

import net.openid.appauth.AuthorizationRequest

sealed class MainViewAction {
    data class LaunchDetail(val osmId: Long) : MainViewAction()
    data class SnackBar(val message: String) : MainViewAction()
    data class InitAuthorization(val authorizationRequest: AuthorizationRequest) : MainViewAction()
}

