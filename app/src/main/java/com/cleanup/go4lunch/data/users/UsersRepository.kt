package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    suspend fun insertUser(user: User): Boolean = try {
        userRetrofit.insertUser(
            UserBody(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                goingAtNoon = user.goingAtNoon
            )
        )

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    suspend fun getUsersList(): List<User>? {
        val response = userRetrofit.getUsers()
        if (!response.isSuccessful) return null
        return response.body()!!.mapNotNull { toUser(it) }
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

    suspend fun getLikedPlaceIds(): List<Long>? {
        val response = userRetrofit.getLikedPlaceIds()
        if (!response.isSuccessful) return null
        return response.body()
    }

    suspend fun getVisitedPlaceIds(): List<Long>? {
        val response = userRetrofit.getVisitedPlaceIds()
        if (!response.isSuccessful) return null
        return response.body()
    }

}
