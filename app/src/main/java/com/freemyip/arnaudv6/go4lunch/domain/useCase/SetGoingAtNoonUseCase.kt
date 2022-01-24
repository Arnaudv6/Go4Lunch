package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.*
import javax.inject.Inject

class SetGoingAtNoonUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    coroutineScope: CoroutineScope,
    dispatchers: AllDispatchers
) : InterpolationUseCase<Boolean>(coroutineScope, dispatchers) {

    suspend fun invoke(user: User, placeId: Long, goingAtNoon: Boolean) {
        interpolate(goingAtNoon) {
            if (goingAtNoon) {
                usersRepository.setGoingAtNoon(user.email, null)
            } else {
                usersRepository.setGoingAtNoon(user.email, placeId)
            }
        }
    }
}
