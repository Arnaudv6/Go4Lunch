package com.freemyip.arnaudv6.go4lunch.data.users

import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(private val userRetrofit: UserRetrofit) {

    private val matesListMutableSharedFlow = MutableSharedFlow<List<User>>(replay = 1).apply {
        tryEmit(emptyList())
    }
    val matesListFlow: Flow<List<User>> = matesListMutableSharedFlow.asSharedFlow()

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
        matesListMutableSharedFlow.tryEmit(
            userRetrofit.getUsers().body()?.mapNotNull { toUser(it) } ?: emptyList()
        )

        likedPlacesFlow.emit(
            userRetrofit.getLikedPlaceIds().body()
                ?.map { it.likedPlaceId }?.toLongArray()
        )

        visitedPlacesFlow.emit(
            userRetrofit.getVisitedPlaceIds().body()
                ?.map { it.visitedId }?.toLongArray()
        )
    }

    suspend fun insertUser(user: User) = userRetrofit.insertUser(
        UserBody(user.email, user.firstName, user.lastName, user.avatarUrl, user.goingAtNoon)
    )

    suspend fun insertLiked(email: String, osmId: Long) {
        if (userRetrofit.insertLiked(email, osmId).isSuccessful) refreshUser(email)
    }

    suspend fun deleteLiked(email: String, osmId: Long) {
        if (userRetrofit.deleteLiked(
                UserRetrofit.EqualEmailId(email),
                UserRetrofit.EqualId(osmId)
            ).isSuccessful
        ) refreshUser(email)
    }

    suspend fun setGoingAtNoon(userId: String, osmId: Long?) {
        if (userRetrofit.setGoingAtNoon(
                UserRetrofit.EqualEmailId(userId),
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

    private suspend fun refreshUser(userId: String) {
        val user = toUser(userRetrofit.getUserById(UserRetrofit.EqualEmailId(userId)).body())
        // execution order matters as getUserById() is suspend and list is heavy on memory
        val list =
            ArrayList(matesListMutableSharedFlow.replayCache.first().filter { it.email != userId })
        list.add(user)
        matesListMutableSharedFlow.tryEmit(list)
    }

    private fun toUser(userResponse: UserResponse?): User? =
        if (
            userResponse?.email != null
            && userResponse.firstName != null
            && userResponse.lastName != null
        ) User(
            email = userResponse.email,
            firstName = userResponse.firstName,
            lastName = userResponse.lastName,
            avatarUrl = userResponse.avatarUrl,
            goingAtNoon = userResponse.goingAtNoon
        ) else null

    suspend fun getLikedById(userId: String) = userRetrofit
        .getLikedById(UserRetrofit.EqualEmailId(userId)).body()?.map { it.likedPlaceId }
        ?.toLongArray()

}

