package com.cleanup.go4lunch.data.users

import com.google.gson.annotations.SerializedName

data class UserVisitedResult(

    @SerializedName("visitedid")
    val id: Long,

    @SerializedName("userid")
    val userId: Long,

    @SerializedName("visitedplaceid")
    val placeId: Long

)
