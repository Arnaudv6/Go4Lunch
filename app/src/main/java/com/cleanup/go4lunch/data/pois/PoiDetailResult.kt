package com.cleanup.go4lunch.data.pois

import com.google.gson.annotations.SerializedName

data class PoiDetailResult(
    @SerializedName("place_id")
    val placeId: Long?,

    @SerializedName("osm_id")
    val osmId: Long?,

    @SerializedName("extratags")
    val extraTags: ExtraTags?,
)

data class ExtraTags(
    val cuisine: String?,
    val phone: String?,
    val website: String?,

    @SerializedName("opening_hours")
    val hours: String?
)

