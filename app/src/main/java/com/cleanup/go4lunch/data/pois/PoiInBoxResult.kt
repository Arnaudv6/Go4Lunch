package com.cleanup.go4lunch.data.pois

import com.google.gson.annotations.SerializedName

data class PoiInBoxResult(
    @SerializedName("place_id")
    val placeId: Long?,

    @SerializedName("osm_id")
    val osmId: Long?,

    val lat: Double?,

    val lon: Double?,

    @SerializedName("display_name")
    val displayName: String?,

    val category: String?, // amenity

    val type: String // restaurant

)
