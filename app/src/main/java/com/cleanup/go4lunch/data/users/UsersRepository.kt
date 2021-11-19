package com.cleanup.go4lunch.data.users

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    // this list also depends on current hour for goingAtNoon
    suspend fun updateMatesList() {
        userRetrofit.getUsers().body()?.mapNotNull { toUser(it) }?.let {
            matesListMutableStateFlow.tryEmit(it)
        }
    }

    suspend fun insertUser(user: User) = userRetrofit.insertUser(
        UserBody(user.id, user.firstName, user.lastName, user.avatarUrl, user.goingAtNoon)
    )

    // we don't refresh on Liked
    suspend fun insertLiked(userId: Long, osmId: Long): Boolean =
        userRetrofit.insertLiked(
            UserRetrofit.EqualId(userId),
            UserRetrofit.EqualId(osmId)
        ).isSuccessful

    // we don't refresh on Liked
    suspend fun deleteLiked(userId: Long, osmId: Long): Boolean =
        userRetrofit.deleteLiked(
            UserRetrofit.EqualId(userId),
            UserRetrofit.EqualId(osmId)
        ).isSuccessful

    suspend fun setGoingAtNoon(userId: Long, osmId: Long?) {
        if (userRetrofit.setGoingAtNoon(
                UserRetrofit.EqualId(userId),
                UserRetrofit.NullableLong(osmId)
            ).isSuccessful
        ) refreshUser(userId)
    }

    private suspend fun refreshUser(userId: Long) {
        val user = toUser(userRetrofit.getUserById(UserRetrofit.EqualId(userId)).body())
        // going in this order as getUserById() is suspend and list is heavy on memory
        val list = ArrayList(matesListMutableStateFlow.value.filter { it.id != userId })
        list.add(user)
        matesListMutableStateFlow.tryEmit(list)
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

    suspend fun getLikedById(userId: Long): LongArray? =
        userRetrofit.getLikedById(UserRetrofit.EqualId(userId)).body()?.toLongArray()

    suspend fun getLikedPlaceIds(): LongArray? =
        userRetrofit.getLikedPlaceIds().body()?.toLongArray()

    suspend fun getVisitedPlaceIds(): LongArray? =
        userRetrofit.getVisitedPlaceIds().body()?.toLongArray()

}

