package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMatesByPlaceUseCase @Inject constructor(private val usersRepository: UsersRepository) {

    operator fun invoke(): Flow<Map<Long, ArrayList<User>>> = usersRepository.matesListFlow.map {
        buildMap {
            for (user in it) {
                user.goingAtNoon?.let { placeId ->
                    this[placeId]?.add(user) ?: put(placeId, ArrayList(listOf(user)))
                }
            }
        }
    }
}

