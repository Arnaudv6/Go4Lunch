package com.cleanup.go4lunch.data.users

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserRetrofit {
    companion object {
        private const val USERS = "users"
        private const val VISITED = "visited_places"
        private const val LIKED = "likedplaces"
    }

    @GET(USERS)
    suspend fun getUsers(): List<UserResponse>

    @GET(VISITED)
    suspend fun getVisited(): List<UserVisitedResponse>

    @GET(LIKED)
    suspend fun getLiked(): List<UserLikedResponse>

    @Headers(
        "Prefer: resolution=merge-duplicates",
        // "Content-Type: application/x-www-form-urlencoded"
    )
    @POST(USERS)
    suspend fun insertUser(@Body userBody: UserBody)

    // todo
    suspend fun toggleLiked(userId: Long, osmId: Long)

    // todo
    suspend fun setGoingAtNoon(userId: Long, osmId: Long)

    // todo
    suspend fun addVisited(userId: Long, osmId: Long)

}
