package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.session.SessionRepository
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionUserUseCase
@Inject constructor(
    sessionRepository: SessionRepository,
    usersRepository: UsersRepository,
) {
    val sessionUserFlow: Flow<SessionUser?> = combine(
        sessionRepository.sessionFlow.filterNotNull(),
        usersRepository.matesListFlow
    ) { session, mates ->
        SessionUser(
            mates.first { it.id == session.userId },
            usersRepository.getLikedById(session.userId) ?: LongArray(0),
            session.connectionType
        )
    }

    /* for a single-function use case, this syntax allows to call UseCaseClass() directly
    suspend operator fun invoke() {
        usersRepository.getUsersList()?.let { matesListMutableStateFlow.tryEmit(it) }
    }
    */
}

