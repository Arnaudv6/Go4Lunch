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
    suspend fun getUsers(): List<UserResult>

    @GET(VISITED)
    suspend fun getVisited(): List<UserVisitedResult>

    @GET(LIKED)
    suspend fun getLiked(): List<UserLikedResult>

    @Headers(
        "Prefer: resolution=merge-duplicates",
        "Content-Type: application/x-www-form-urlencoded"
    )
    @POST(USERS)
    suspend fun insertUser(@Body userBody: UserBody)

}
