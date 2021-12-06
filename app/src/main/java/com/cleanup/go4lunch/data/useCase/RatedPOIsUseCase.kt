package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatedPOIsUseCase @Inject constructor(
    usersRepository: UsersRepository,
) {
    val placesIdRatingsFlow: Flow<HashMap<Long, Int>> = flow {
        // following two suspend functions would delay first value by much
        // todo Nino : callback-flow? launch? stateIn? c'est suspend et memory-heavy
        val visited = usersRepository.getVisitedPlaceIds() ?: LongArray(0)
        val liked = usersRepository.getLikedPlaceIds() ?: LongArray(0)

        val placesIdRatings = HashMap<Long, Int>()
        for (place in (visited + liked).toSet()) {
            // avoid division by zero.
            val ratio = liked.count { it == place } / (visited.count { it == place }.toFloat() + .1)
            placesIdRatings[place] = when {
                ratio < 0.2 -> 1
                ratio < 0.3 -> 2
                else -> 3
            }
        }
        emit(placesIdRatings)
    }
}

