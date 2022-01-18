package com.freemyip.go4lunch.data.useCase

import com.freemyip.go4lunch.data.users.User
import com.freemyip.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatesByPlaceUseCase @Inject constructor(usersRepository: UsersRepository) {

    val matesByPlaceFlow: Flow<Map<Long, ArrayList<User>>> = usersRepository.matesListFlow.map {
        val map = HashMap<Long, ArrayList<User>>()
        for (user in it) {
            user.goingAtNoon?.let { placeId ->
                map[placeId]?.add(user) ?: map.put(placeId, ArrayList(listOf(user)))
            }
        }
        map
    }
}

