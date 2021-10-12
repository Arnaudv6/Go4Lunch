package com.cleanup.go4lunch.data.pois

import com.google.gson.annotations.SerializedName

data class PoiInBoxResult(
    val html_attributions: List<Any>,
    val next_page_token: String,
    val results: List<Item>,
    val status: String
)


data class Item(
    @SerializedName("place_id")
    val placeId: Int?,

    @SerializedName("osm_id")
    val osmId: Int?,

    val lat: Double?,

    val lon: Double?,

    @SerializedName("display_name")
    val displayName: String?,
)
