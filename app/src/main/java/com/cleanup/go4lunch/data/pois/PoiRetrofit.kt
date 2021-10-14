package com.cleanup.go4lunch.data.pois

import com.cleanup.go4lunch.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PoiRetrofit {
    companion object {
        private const val SEARCH_IN_BOX = "search"
        private const val SEARCH_DETAILS = "details"
    }

// https://nominatim.openstreetmap.org/search?viewbox=-0.91187%2C46.74362%2C6.06445%2C44.99200&format=json&q=[restaurant]&limit=20&bounded=1

    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(SEARCH_IN_BOX)
    suspend fun getPoiInBox(
        @Query("q") query: String = "[restaurant]",
        @Query("viewbox") viewBox: String, // "<x1>,<y1>,<x2>,<y2>"
        @Query("bounded") bounded: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "jsonv2",  // [xml|json|jsonv2|geojson|geocodejson]
        @Query("accept-language") lang: String = "EN",  // "fr"
        // @Query("email") email: String = EMAIL,
    ): List<PoiInBoxResult>?

    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(SEARCH_DETAILS)
    suspend fun getPoiDetails(
        @Query("osmtype") osmType: String = "N",
        @Query("osmid") osmId: Long,
        @Query("class") itemClass: String = "amenity",
        @Query("linkedplaces") linkedPlaces: Int = 0,
        @Query("format") format: String = "json",
        @Query("accept-language") lang: String = "EN",  // "fr"
    ): PoiDetailResult?

}
