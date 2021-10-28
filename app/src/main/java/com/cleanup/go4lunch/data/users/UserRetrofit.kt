package com.cleanup.go4lunch.data.users

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserRetrofit {
    companion object {
        private const val USERS = "users/"
        private const val VISITED = "visited_places/"
        private const val LIKED = "likedplaces/"
    }

    @GET(USERS)
    suspend fun getUsers(): List<UserResult>

    @GET(VISITED)
    suspend fun getVisited(): List<UserVisitedResult>

    @GET(LIKED)
    suspend fun getLiked(): List<UserLikedResult>

    @POST(USERS)
    suspend fun insertUser(
        @Query("id") id: Long,
        @Query("firstname") firstName: String,
        @Query("lastname") LastName: String,
        @Query("avatarurl") avatarUrl: String,
        @Query("goingatnoon") goingAtNoon: Long,
    ): List<UserResult>

}
