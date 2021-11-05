package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    // todo decorator java.net.ConnectException
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

    suspend fun getUsersList(): List<User> {
        return userRetrofit.getUsers().mapNotNull { toUser(it) }
    }

    suspend fun toggleLiked(userId: Long, osmId: Long) =
        userRetrofit.toggleLiked(userId, osmId)

    suspend fun setGoingAtNoon(userId: Long, osmId: Long) =
        userRetrofit.setGoingAtNoon(userId, osmId)

    suspend fun getSessionUser(userId: Long): User? {
        val response = userRetrofit.getUserById(UserRetrofit.EqualId(userId))
        if (!response.isSuccessful) return null
        return toUser(response.body())
    }

    private fun toUser(userResponse: UserResponse?): User? =
        if (
            userResponse?.id != null
            && userResponse.firstName != null
            && userResponse.lastName != null
        ) User(
            id = userResponse.id,
            firstName = userResponse.firstName,
            lastName = userResponse.lastName,
            avatarUrl = userResponse.avatarUrl,
            goingAtNoon = userResponse.goingAtNoon
        ) else null

    suspend fun getLikedPlaceIds(): List<Long> = userRetrofit.getLikedPlaceIds()

    suspend fun getVisitedPlaceIds(): List<Long> = userRetrofit.getVisitedPlaceIds()

}
