package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.session.SessionRepository
import com.freemyip.arnaudv6.go4lunch.data.session.SessionUser
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
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
    // TODO invoke plutôt
    val sessionUserFlow: Flow<SessionUser?> = combine(
        sessionRepository.sessionFlow,
        usersRepository.matesListFlow
    ) { session, mates ->
        if (session == null) null
        else mates.firstOrNull { it.id == session.userId }.let {
            when (it) {
                is User -> SessionUser(
                    user = it,
                    liked = usersRepository.getLikedById(it.id) ?: LongArray(0),
                    connectedThrough = session.connectionType
                )
                else -> null
            }
        }
    }

    /* for a single-function use case, this syntax allows to call UseCaseClass() directly
    suspend operator fun invoke() {
        usersRepository.getUsersList()?.let { matesListMutableStateFlow.tryEmit(it) }
    }
    */
}

