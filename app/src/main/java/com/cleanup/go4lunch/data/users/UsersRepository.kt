package com.cleanup.go4lunch.data.users

import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    private val visitedPlacesFlow = MutableStateFlow<LongArray?>(longArrayOf())
    private val likedPlacesFlow = MutableStateFlow<LongArray?>(longArrayOf())

    val placesRatingsFlow: Flow<Map<Long, Int>> = combine(
        visitedPlacesFlow.filterNotNull(),
        likedPlacesFlow.filterNotNull()
    ) { visited, liked ->
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
        placesIdRatings
    }

    // this list could also depends on current hour (for goingAtNoon)
    suspend fun updateMatesList() {
        matesListMutableStateFlow.value =
            userRetrofit.getUsers().body()?.mapNotNull { toUser(it) } ?: emptyList()
        getLikedPlaceIds()
        getVisitedPlaceIds()
    }

    suspend fun insertUser(user: User) = userRetrofit.insertUser(
        UserBody(user.id, user.firstName, user.lastName, user.avatarUrl, user.goingAtNoon)
    )

    suspend fun insertLiked(userId: Long, osmId: Long) {
        if (userRetrofit.insertLiked(userId, osmId).isSuccessful) refreshUser(userId)
    }

    suspend fun deleteLiked(userId: Long, osmId: Long) {
        if (userRetrofit.deleteLiked(
                UserRetrofit.EqualId(userId),
                UserRetrofit.EqualId(osmId)
            ).isSuccessful
        ) refreshUser(userId)
    }

    suspend fun setGoingAtNoon(userId: Long, osmId: Long?) {
        if (userRetrofit.setGoingAtNoon(
                UserRetrofit.EqualId(userId),
                UserRetrofit.NullableLong(osmId)
            ).isSuccessful
        /*
        when (osmId) {
            null -> userRetrofit.delGoingAtNoon(
                UserRetrofit.EqualId(userId),
                UserRetrofit.NullableLong(null)
            )
            else -> userRetrofit.setGoingAtNoon(
                UserRetrofit.EqualId(userId),
                UserRetrofit.NullableLong(osmId)
            )
        }.isSuccessful
        */
        ) refreshUser(userId)
    }

    private suspend fun refreshUser(userId: Long) {
        val user = toUser(userRetrofit.getUserById(UserRetrofit.EqualId(userId)).body())
        // execution order matters as getUserById() is suspend and list is heavy on memory
        val list = ArrayList(matesListMutableStateFlow.value.filter { it.id != userId })
        list.add(user)
        matesListMutableStateFlow.value = list
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

    suspend fun getLikedById(userId: Long): LongArray? = userRetrofit
        .getLikedById(UserRetrofit.EqualId(userId)).body()?.map { it.likedPlaceId }?.toLongArray()

    // retry + delay loop?
    private suspend fun getLikedPlaceIds() = likedPlacesFlow
        .emit(userRetrofit.getLikedPlaceIds().body()?.map { it.likedPlaceId }?.toLongArray())

    private suspend fun getVisitedPlaceIds() = visitedPlacesFlow
        .emit(userRetrofit.getVisitedPlaceIds().body()?.map { it.visitedId }?.toLongArray())

}

