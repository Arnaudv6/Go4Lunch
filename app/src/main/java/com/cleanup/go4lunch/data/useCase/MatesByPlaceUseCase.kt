package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatesByPlaceUseCase @Inject constructor(usersRepository: UsersRepository) {
    val matesByPlaceFlow : Flow<HashMap<Long, ArrayList<String>>> = usersRepository.matesListFlow.map {
        val map = HashMap<Long, ArrayList<String>>()
        for (user in it){
            if(user.goingAtNoon != null && map.containsKey(user.goingAtNoon))
                map[user.goingAtNoon]?.add(user.firstName)
                    ?: map.put(user.goingAtNoon, ArrayList(listOf(user.firstName)))
        }
        map
    }
}

