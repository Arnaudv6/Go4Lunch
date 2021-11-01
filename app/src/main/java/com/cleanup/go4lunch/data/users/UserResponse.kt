package com.cleanup.go4lunch.data.users

import com.google.gson.annotations.SerializedName

data class UserResponse(

    val id: Long,

    @SerializedName("firstname")
    val firstName: String,

    @SerializedName("lastname")
    val lastName: String,

    @SerializedName("avatarurl")
    val avatarUrl: String,

    @SerializedName("goingatnoon")
    val goingAtNoon: Long

)
