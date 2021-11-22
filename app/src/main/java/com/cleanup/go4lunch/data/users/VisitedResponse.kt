package com.cleanup.go4lunch.data.users

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VisitedResponse(

    @SerializedName("visitedid")
    val visitedId: Long,

    @SerializedName("userid")
    val userId: Long,

    @SerializedName("visitedplaceid")
    val visitedPlaceId: Long
)

