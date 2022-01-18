package com.freemyip.arnaudv6.go4lunch.data.users

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LikedResponse(

    @SerializedName("likedid")
    val likedId: Long,

    @SerializedName("userid")
    val userId: Long,

    @SerializedName("likedplaceid")
    val likedPlaceId: Long
)

