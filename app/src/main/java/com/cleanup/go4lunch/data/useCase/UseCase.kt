package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.SessionUser
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UseCase
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository
) {
    val sessionUserFlow: Flow<SessionUser?> = settingsRepository.idStateFlow.map {
        if (it == null) {
            null
        } else {
            val user = usersRepository.getSessionUser(it)
            if (user == null) null
            else SessionUser(
                user,
                longArrayOf(),// todo usersRepository.getLikedById(it).toLongArray(),
                settingsRepository.getConnectionType()
            )
        }
    }

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    // todo: fetch all goingAtNoon POIs?
    suspend fun updateUsers() {
        val list = usersRepository.getUsersList()
        if (list != null) matesListMutableStateFlow.tryEmit(list)
    }

    private suspend fun updateRatings() {
        val visited = usersRepository.getVisitedPlaceIds().orEmpty()
        val liked = usersRepository.getLikedPlaceIds().orEmpty()
        for (place in visited.toSet()) {
            val ratio = liked.count { it == place } / visited.count { it == place }.toFloat()
            poiRepository.updatePoiRating(
                place, when {
                    ratio < 0.2 -> 1
                    ratio < 0.3 -> 2
                    else -> 3
                }
            )
        }
    }

    fun usersGoingThere(osmId: Long): List<User> = matesListMutableStateFlow
        .value.filter { it.goingAtNoon == osmId }

}


