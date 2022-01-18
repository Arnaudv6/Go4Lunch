package com.freemyip.go4lunch.data.pois

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PoiResponse(
    // those can be generated from json by plugins
    @SerializedName("place_id")
    val placeId: Long?,

    @SerializedName("osm_id")
    val osmId: Long?,

    val lat: Double?,
    val lon: Double?,
    val address: Address?,

    @SerializedName("extratags")
    val extraTags: ExtraTags?,

    val category: String?, // amenity
    val type: String? // restaurant
) {
    data class Address(
        val amenity: String?,

        @SerializedName("house_number")
        val number: String?,

        val road: String?,
        val municipality: String?,
        val postcode: String?
    )

    data class ExtraTags(
        val cuisine: String?,
        val phone: String?,
        val website: String?,

        @SerializedName("opening_hours")
        val hours: String?
    )
}

