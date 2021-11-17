package com.cleanup.go4lunch.data.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor() {
    // Todo Nino : est-ce qu'un repository peut dépendre d'un autre, ou c'est le moment d'emerger un usecase?
    val sessionFlow: Flow<Session?> = flow { emit(Session(1, "gmail")) }
}

