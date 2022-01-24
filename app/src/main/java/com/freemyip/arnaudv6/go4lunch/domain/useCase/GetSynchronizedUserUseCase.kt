package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.ConnectivityRepository
import com.freemyip.arnaudv6.go4lunch.data.SessionRepository
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class GetSynchronizedUserUseCase
@Inject constructor(
    private val sessionRepository: SessionRepository,
    private val usersRepository: UsersRepository,
    private val connectivityRepository: ConnectivityRepository
) {
    private var user: User? = null

    // todo observe sessionRepository.userInfoFlow, to create user if un-existent...
    //  and populate liked = usersRepository.getLikedById(it.email) ?: LongArray(0)
    operator fun invoke(): Flow<User?> = combine(
        sessionRepository.userInfoFlow,
        usersRepository.matesListFlow.filterNotNull()
    ) { maybeSessionUser, dbUsersList ->
        maybeSessionUser?.let { sessionUser ->

            withTimeoutOrNull(2_000) {
                if (dbUsersList.firstOrNull { it.isSameId(sessionUser) } == null)
                    usersRepository.insertUser(sessionUser)
            }

            var liked: LongArray? = null
            // maybe surround this with a loop?
            withTimeoutOrNull(2_000) { liked = usersRepository.getLikedById(sessionUser.email) }

            User(
                sessionUser.email,
                sessionUser.firstName,
                sessionUser.lastName,
                sessionUser.avatarUrl,
                sessionUser.goingAtNoon,
                liked
            )
        }
    }

}

