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

    @Headers("Prefer: resolution=merge-duplicates")
    @POST(USERS)
    suspend fun insertUser(@Body userBody: UserBody)

    @FormUrlEncoded
    @PATCH(USERS)
    suspend fun setGoingAtNoon(
        @Query("id") userId: EqualId,
        @Field("goingatnoon") osmId: Long
    ): Response<Unit>  // response needed for interpolation

    @GET(VISITED)
    suspend fun getVisitedPlaceIds(): Response<List<Long>>

    // todo or must server itself add visited?
    suspend fun addVisited(userId: Long, osmId: Long)

    @GET(LIKED)
    suspend fun getLikedPlaceIds(): Response<List<Long>>

    @GET(LIKED_BY_ID)
    suspend fun getLikedById(@Query("id") userId: EqualId): Response<List<Long>>

    @Headers("Prefer: resolution=merge-duplicates")
    @POST(LIKED)
    suspend fun insertLiked(
        @Query("userid") userId: EqualId,
        @Query("likedplaceid") osmId: EqualId
    ): Response<Unit>  // response needed for interpolation

    @DELETE(LIKED)
    suspend fun deleteLiked(
        @Query("userid") userId: EqualId,
        @Query("likedplaceid") osmId: EqualId
    ): Response<Unit>  // response needed for interpolation

    data class EqualId(val id: Long) {
        override fun toString() = "eq.$id"
    }

}
