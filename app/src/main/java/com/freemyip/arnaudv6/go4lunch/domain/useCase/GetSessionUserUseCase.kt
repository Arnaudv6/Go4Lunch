package com.freemyip.arnaudv6.go4lunch.domain.useCase

import android.util.Log
import com.freemyip.arnaudv6.go4lunch.data.ConnectivityRepository
import com.freemyip.arnaudv6.go4lunch.data.session.Session
import com.freemyip.arnaudv6.go4lunch.data.session.SessionRepository
import com.freemyip.arnaudv6.go4lunch.data.session.SessionUser
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class GetSessionUserUseCase
@Inject constructor(
    private val sessionRepository: SessionRepository,
    private val usersRepository: UsersRepository,
    private val connectivityRepository: ConnectivityRepository
) {
    operator fun invoke(): Flow<SessionUser?> =
        combine(
            connectivityRepository.isNetworkAvailableFlow,
            sessionRepository.userInfoFlow,
            usersRepository.matesListFlow
        ) { _, session, mates -> // not using network info (yet)
            if (session == null) null
            else {
                Log.e("TAG", "invoke: $session")
                // todo change this for a useful code !
                val session2 = Session(1, "gmail")

                mates.firstOrNull { it.id == session2.userId }.let {
                    when (it) {
                        is User -> SessionUser(
                            user = it,
                            liked = usersRepository.getLikedById(it.id) ?: LongArray(0),
                            connectedThrough = session2.connectionType
                        )
                        else -> null
                    }
                }
            }
        }
}

