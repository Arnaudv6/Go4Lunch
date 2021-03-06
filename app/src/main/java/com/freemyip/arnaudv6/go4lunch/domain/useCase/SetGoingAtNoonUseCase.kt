package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class SetGoingAtNoonUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    dispatchers: AllDispatchers
// this could depend on GetSynchronizedUserUseCase...
) : InterpolationUseCase<Boolean>(dispatchers) {

    suspend operator fun invoke(user: User, placeId: Long) {
        val initialState = user.goingAtNoon == placeId
        interpolate(!initialState) {
            if (initialState) {
                usersRepository.setGoingAtNoon(user.email, null)
            } else {
                usersRepository.setGoingAtNoon(user.email, placeId)
            }
        }
    }
}
