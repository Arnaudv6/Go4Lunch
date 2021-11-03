package com.cleanup.go4lunch.data

import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.SessionUser
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.exhaustive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
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
                usersRepository.getLikedById(it).toLongArray(),
                settingsRepository.getConnectionType()
            )
        }
    }

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    suspend fun updateUsers() {
        matesListMutableStateFlow.tryEmit(usersRepository.getUsersList())
    }

    // todo Nino : on fait passe-plat pour tout ? les VM ne parlent plus aux repo?
    suspend fun getPoiById(osmId: Long): PoiEntity? = poiRepository.getPoiById(osmId)

    val cachedPOIsListFlow: Flow<List<PoiEntity>> = poiRepository.cachedPOIsListFlow

    private suspend fun updateRatings() {
        val visited = usersRepository.getVisitedPlaceIds()
        val liked = usersRepository.getLikedPlaceIds()
        for (place in visited.toSet()) {
            val ratio = liked.count { it == place } / visited.count { it == place }.toFloat()
            poiRepository.updatePoiRating(
                place, when {
                    ratio < 0.2 -> 1
                    ratio < 0.3 -> 2
                    else -> 3
                }.exhaustive
            )
        }
    }

    fun usersGoingThere(osmId: Long): List<User> = matesListMutableStateFlow
        .value.filter { it.goingAtNoon == osmId }

    suspend fun fetchPOIsInBoundingBox(boundingBox: BoundingBox): Int =
        poiRepository.fetchPOIsInBoundingBox(boundingBox)

    suspend fun getInitialBox(): BoundingBox = settingsRepository.getInitialBox()

    fun setMapBox(boxEntity: BoxEntity) = settingsRepository.setMapBox(boxEntity)

}


