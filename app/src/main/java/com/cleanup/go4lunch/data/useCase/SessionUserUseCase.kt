package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.session.Session
import com.cleanup.go4lunch.data.session.SessionRepository
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionUserUseCase
@Inject constructor(
    sessionRepository: SessionRepository,
    usersRepository: UsersRepository,
) {
    val sessionUserFlow: Flow<SessionUser?> = sessionRepository.sessionFlow.map {
        it?.let { session: Session ->
            usersRepository.getSessionUser(session.userId)?.let { user: User ->
                SessionUser(
                    user,
                    usersRepository.getLikedById(session.userId) ?: LongArray(0),
                    session.connectionType
                )
            }
        }
    }

    /* for a single-function use case, this syntax allows to call UseCaseClass() directly
    suspend operator fun invoke() {
        usersRepository.getUsersList()?.let { matesListMutableStateFlow.tryEmit(it) }
    }
    */
}

