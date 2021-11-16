package com.cleanup.go4lunch.data.users

import retrofit2.Response
import retrofit2.http.*

interface UserRetrofit {
    companion object {
        private const val USERS = "users"
        private const val VISITED = "visited_places"
        private const val LIKED = "likedplaces"
        private const val LIKED_BY_ID = "likedplaces?select="
    }

    @Headers("Accept: application/vnd.pgrst.object")
    @GET(USERS)
    suspend fun getUserById(@Query("id") userId: EqualId): Response<UserResponse>

    @GET(USERS)
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET(VISITED)
    suspend fun getVisitedPlaceIds(): Response<List<Long>>

    @GET(LIKED)
    suspend fun getLikedPlaceIds(): Response<List<Long>>

    @GET(LIKED_BY_ID)
    suspend fun getLikedById(@Query("id") userId: EqualId): Response<List<Long>>

    @Headers("Prefer: resolution=merge-duplicates")
    @POST(USERS)
    suspend fun insertUser(@Body userBody: UserBody)

    // todo must return a Boolean, as we do interpolation
    suspend fun toggleLiked(userId: Long, osmId: Long): Response<Unit>

    // todo must return a Boolean, as we do interpolation
    suspend fun setGoingAtNoon(userId: Long, osmId: Long): Response<Unit>

    // todo
    suspend fun addVisited(userId: Long, osmId: Long)

    data class EqualId(val id: Long) {
        override fun toString() = "eq.$id"
    }

}
