package com.cleanup.go4lunch.data.session

import com.cleanup.go4lunch.data.ConnectivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    connectivityRepository: ConnectivityRepository
) {
    // Todo Nino : est-ce qu'un repository peut dépendre d'un autre, ou c'est le moment d'emerger un usecase?
    val sessionFlow: Flow<Session?> = connectivityRepository.isNetworkAvailableFlow.map {
        if (it) Session(1, "gmail") else null
    }
}

