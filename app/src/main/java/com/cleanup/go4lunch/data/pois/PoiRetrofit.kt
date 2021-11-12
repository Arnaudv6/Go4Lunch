package com.cleanup.go4lunch.data.pois

import com.cleanup.go4lunch.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PoiRetrofit {
    companion object {
        private const val SEARCH_IN_BOX = "search"
        private const val LOOKUP = "lookup"
    }

    // beware: Android Studio tends to generate "abstract", non-suspend methods. We want the opposite

    // https://nominatim.openstreetmap.org/search?viewbox=-0.91187%2C46.74362%2C6.06445%2C44.99200&format=json&q=[restaurant]&limit=20&bounded=1
    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(SEARCH_IN_BOX)
    suspend fun getPoiInBoundingBox(
        @Query("q") query: String = "[restaurant]",
        @Query("viewbox") viewBox: String, // "<x1>,<y1>,<x2>,<y2>"
        @Query("bounded") bounded: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "jsonv2",  // [xml|json|jsonv2|geojson|geocodejson]
        @Query("accept-language") lang: String = "EN",  // "fr"
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
        // have to specify either a mail, or User-Agent Header
        // @Query("email") email: String = EMAIL,
    ): Response<List<PoiInBoxResponse>>

    // https://nominatim.org/release-docs/latest/api/Lookup/
    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(LOOKUP)
    suspend fun getPOIsInList(
        @Query("osm_ids", encoded = true) idsLongArray: IdsLongArray,
        @Query("format") format: String = "jsonv2",  // [xml|json|jsonv2|geojson|geocodejson]
        @Query("accept-language") lang: String = "EN",  // "fr"
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
        // have to specify either a mail, or User-Agent Header
        // @Query("email") email: String = EMAIL,
    ): Response<List<PoiInBoxResponse>>

    @Suppress("ArrayInDataClass")
    data class IdsLongArray(val ids: LongArray) {
        override fun toString() = ids.joinToString(transform = { id -> "N${id}" }, separator = ",")
    }
}


