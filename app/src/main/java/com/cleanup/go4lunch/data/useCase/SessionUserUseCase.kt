package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.session.SessionRepository
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionUserUseCase
@Inject constructor(
    sessionRepository: SessionRepository,
    usersRepository: UsersRepository,
) {
    val sessionUserFlow: Flow<SessionUser?> = combine(
        sessionRepository.sessionFlow,
        usersRepository.matesListFlow
    ) { session, mates ->
        if (session == null) null
        else mates.firstOrNull { it.id == session.userId }?.let { user ->
            SessionUser(
                user = user,
                liked = usersRepository.getLikedById(session.userId) ?: LongArray(0),
                connectedThrough = session.connectionType
            )
        }
    }

    /* for a single-function use case, this syntax allows to call UseCaseClass() directly
    suspend operator fun invoke() {
        usersRepository.getUsersList()?.let { matesListMutableStateFlow.tryEmit(it) }
    }
    */
}

