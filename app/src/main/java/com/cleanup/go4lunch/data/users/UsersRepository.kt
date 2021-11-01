package com.cleanup.go4lunch.data.users

import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.exhaustive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val userRetrofit: UserRetrofit,
    private val settingsRepository: SettingsRepository,
) {

    // todo rationalize this crap. What can I stand in memory? what do I want in cache?
    //  or do I out all this complexity to backend?

    // TODO Still necessary ?
    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    suspend fun requestDataRefresh() {
        val liked = userRetrofit.getLiked()
        val visited = userRetrofit.getVisited()
        matesListMutableStateFlow.tryEmit(userRetrofit.getUsers().map { userResponse ->
            toUser(
                userResponse,
                visited.filter { it.userId == userResponse.id }.map { it.placeId },
                liked.filter { it.userId == userResponse.id }.map { it.placeId }
            )
        })
    }

    // todo could not use matesListMutableStateFlow : serpent qui se mort la queue
    val sessionUserFlow: Flow<User?> = settingsRepository.idStateFlow
        .map { userId -> userRetrofit.getUsers().find { it.id == userId }?.let { toUser(it) } }

    private fun toUser(userResponse: UserResponse, visited: List<Long>, liked: List<Long>) = User(
        id = userResponse.id,
        firstName = userResponse.firstName,
        lastName = userResponse.lastName,
        avatarUrl = userResponse.avatarUrl,
        visitedPlaces = visited.toLongArray(),
        likedPlaces = liked.toLongArray(),
        goingAtNoon = userResponse.goingAtNoon
    )

    fun usersGoingThere(osmId: Long): List<User> {
        return matesListMutableStateFlow.value.filter {
            it.goingAtNoon == osmId
        }
    }

    suspend fun insertUser(user: User) {
        userRetrofit.insertUser(
            UserBody(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                goingAtNoon = user.goingAtNoon
            )
        )
    }

    suspend fun getPlaceRating(osmId: Long): Int {
        // this belongs in UseCase.
        val ratio = userRetrofit.getLiked().filter { it.placeId == osmId }.size /
                userRetrofit.getVisited().filter { it.placeId == osmId }.size.toDouble()
        return when {
            ratio < 0.2 -> 1
            ratio < 0.3 -> 2
            else -> 3
        }.exhaustive
    }

    suspend fun likes(userId: Long, osmId: Long) {
        userRetrofit.getLiked().
    }

    suspend fun toggleLiked(userId: Long, osmId: Long) {
        userRetrofit.toggleLiked(userId, osmId)
        requestDataRefresh() // todo : can I do less error prone? less resource heavy?
    }

    suspend fun setGoingAtNoon(userId: Long, osmId: Long) {
        userRetrofit.setGoingAtNoon(userId, osmId)
        requestDataRefresh() // todo : can I do less error prone? less resource heavy?
    }

}
