package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatesByPlaceUseCase @Inject constructor(private val usersRepository: UsersRepository) {

    fun invoke(): Flow<Map<Long, ArrayList<User>>> = usersRepository.matesListFlow.map {
        buildMap {
            for (user in it) {
                val placeId = user.goingAtNoon
                if (placeId != null) {
                    put(placeId, ArrayList(listOf(user)))
                }
            }
        }
    }
}

