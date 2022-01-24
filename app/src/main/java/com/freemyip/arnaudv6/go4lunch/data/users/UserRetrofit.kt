package com.freemyip.arnaudv6.go4lunch.data.users

import retrofit2.Response
import retrofit2.http.*

interface UserRetrofit {
    companion object {
        private const val USERS = "users"
        private const val VISITED = "visited_places"
        private const val LIKED = "likedplaces"
    }

    @Headers("Accept: application/vnd.pgrst.object")
    @GET(USERS)
    suspend fun getUserById(@Query("id") userEmailId: EqualEmailId): Response<UserResponse>

    @GET(USERS)
    suspend fun getUsers(): Response<List<UserResponse>>

    @Headers("Prefer: resolution=merge-duplicates")
    @POST(USERS)
    suspend fun insertUser(@Body userBody: UserBody)

    // todo null gets invisible simple-quoting it seems
    //  (based on postgres message when I run ALTER TABLE command in psql)
    @FormUrlEncoded
    @PATCH(USERS)
    suspend fun setGoingAtNoon(
        @Query("id") userEmailId: EqualEmailId,
        @Field("goingatnoon", encoded = true) osmId: NullableLong
    ): Response<Unit>  // response needed for interpolation


    @Headers("Prefer: resolution=merge-duplicates")
    @FormUrlEncoded
    @POST(USERS)
    suspend fun setGoingAtNoon2(
        @Query("id") userEmailId: EqualEmailId,
        @Field("goingatnoon", encoded = true) osmId: NullableLong  // todo fix that deletion
    ): Response<Unit>  // response needed for interpolation

    @GET(VISITED)
    suspend fun getVisitedPlaceIds(): Response<List<VisitedResponse>>

    // must server itself add visited?
    // suspend fun addVisited(userId: Long, osmId: Long)

    @GET(LIKED)
    suspend fun getLikedPlaceIds(): Response<List<LikedResponse>>

    @GET(LIKED)  // "likedplaces?select=likedplaceid"
    suspend fun getLikedById(@Query("userid") userEmailId: EqualEmailId): Response<List<LikedResponse>>

    @FormUrlEncoded
    @Headers("Prefer: resolution=merge-duplicates")
    // todo fix duplicates. (primary key auto-assigned)
    @POST(LIKED)
    suspend fun insertLiked(
        @Field("userid") userId: String,
        @Field("likedplaceid") osmId: Long
    ): Response<Unit>  // response needed for interpolation

    @DELETE(LIKED)
    suspend fun deleteLiked(
        @Query("userid") userEmailId: EqualEmailId,
        @Query("likedplaceid") osmId: EqualId
    ): Response<Unit>  // response needed for interpolation

    data class EqualId(val id: Long) {
        override fun toString() = "eq.$id"
    }

    data class EqualEmailId(val email: String) {
        override fun toString() = "eq.$email"
    }

    data class NullableLong(val long: Long?) {
        override fun toString() = long?.toString() ?: "null"
        // https://www.postgresql.org/message-id/Pine.LNX.4.33.0402181239030.2832-100000@css120.ihs.com
    }

}
