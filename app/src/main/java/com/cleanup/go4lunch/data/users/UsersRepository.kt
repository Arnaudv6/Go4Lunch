package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    suspend fun insertUser(user: User) = userRetrofit.insertUser(
        UserBody(user.id, user.firstName, user.lastName, user.avatarUrl, user.goingAtNoon)
    )

    suspend fun getUsersList(): List<User>? = // null if request goes wrong
        userRetrofit.getUsers().body()?.mapNotNull { toUser(it) }

    suspend fun insertLiked(userId: Long, osmId: Long): Boolean =
        userRetrofit.insertLiked(
            UserRetrofit.EqualId(userId),
            UserRetrofit.EqualId(osmId)
        ).isSuccessful

    suspend fun deleteLiked(userId: Long, osmId: Long): Boolean =
        userRetrofit.deleteLiked(
            UserRetrofit.EqualId(userId),
            UserRetrofit.EqualId(osmId)
        ).isSuccessful

    suspend fun setGoingAtNoon(userId: Long, osmId: Long): Boolean =
        userRetrofit.setGoingAtNoon(
            UserRetrofit.EqualId(userId),
            osmId
        ).isSuccessful

    suspend fun getSessionUser(userId: Long): User? =
        toUser(userRetrofit.getUserById(UserRetrofit.EqualId(userId)).body())

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

    suspend fun getLikedById(userId: Long): LongArray? =
        userRetrofit.getLikedById(UserRetrofit.EqualId(userId)).body()?.toLongArray()

    suspend fun getLikedPlaceIds(): LongArray? =
        userRetrofit.getLikedPlaceIds().body()?.toLongArray()

    suspend fun getVisitedPlaceIds(): LongArray? =
        userRetrofit.getVisitedPlaceIds().body()?.toLongArray()

}

