package com.freemyip.arnaudv6.go4lunch.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.freemyip.arnaudv6.go4lunch.data.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    sessionRepository: SessionRepository
): ViewModel() {
    val authorizationRequestLiveData = sessionRepository.authorizationRequestFlow.asLiveData()
}

