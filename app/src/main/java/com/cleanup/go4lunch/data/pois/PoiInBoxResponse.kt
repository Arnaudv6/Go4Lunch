package com.cleanup.go4lunch.data.pois

import com.google.gson.annotations.SerializedName

data class PoiInBoxResponse(
    // serialized_name needed everywhere for minified releases
    // todo step back if it does not avail
    @SerializedName("place_id")
    val placeId: Long?,

    @SerializedName("osm_id")
    val osmId: Long?,

    @SerializedName("lat")
    val lat: Double?,

    @SerializedName("lon")
    val lon: Double?,

    @SerializedName("address")
    val address: Address?,

    @SerializedName("extratags")
    val extraTags: ExtraTags?,

    @SerializedName("category")
    val category: String?, // amenity

    @SerializedName("type")
    val type: String // restaurant
) {
    data class Address(
        @SerializedName("amenity")
        val amenity: String?,

        @SerializedName("house_number")
        val number: String?,

        @SerializedName("road")
        val road: String?,

        @SerializedName("municipality")
        val municipality: String?,

        @SerializedName("postcode")
        val postcode: String?
    )

    data class ExtraTags(
        @SerializedName("cuisine")
        val cuisine: String?,

        @SerializedName("phone")
        val phone: String?,

        @SerializedName("website")
        val website: String?,

        @SerializedName("opening_hours")
        val hours: String?
    )
}

