package com.cleanup.go4lunch.data.users

import com.google.gson.annotations.SerializedName

data class UserLikedResult(

    @SerializedName("likedid")
    val id: Long,

    @SerializedName("userid")
    val userId: Long,

    @SerializedName("likedplaceid")
    val placeId: Long

)

