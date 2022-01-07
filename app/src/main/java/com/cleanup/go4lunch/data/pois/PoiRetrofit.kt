package com.cleanup.go4lunch.data.pois

import com.cleanup.go4lunch.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PoiRetrofit {
    companion object {
        private const val SEARCH = "search"
        private const val LOOKUP = "lookup"
    }

    // Android Studio tends to generate "abstract", non-suspend methods. We want the opposite

    // https://nominatim.openstreetmap.org/search?viewbox=-0.91187%2C46.74362%2C6.06445%2C44.99200&format=json&q=[restaurant]&limit=20&bounded=1
    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(SEARCH)
    suspend fun getPoiInBoundingBox(
        @Query("q") query: String = "[restaurant]",
        @Query("viewbox") viewBox: String,
        @Query("bounded") bounded: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "jsonv2",  // [xml|json|jsonv2|geojson|geocodejson]
        @Query("accept-language") lang: String = "EN",  // "fr" for localization
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
        // @Query("email") email: String = EMAIL,  // should we rather give a mail than a user-agent
    ): Response<List<PoiResponse>>

    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(LOOKUP)
    suspend fun getPOIsInList(
        @Query("osm_ids", encoded = true) idsLongArray: IdsLongArray,
        @Query("format") format: String = "jsonv2",
        @Query("accept-language") lang: String = "EN",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
    ): Response<List<PoiResponse>>

    // todo restrain to exact matches : "ravioli" will return random results, not shown with filter, which feels like a bug
    @Headers("User-Agent: ${BuildConfig.APPLICATION_ID}")
    @GET(SEARCH)
    suspend fun getPoiByName(
        @Query("q") query: RestaurantName,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "jsonv2",
        @Query("accept-language") lang: String = "EN",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
    ): Response<List<PoiResponse>>

    @Suppress("ArrayInDataClass")
    data class IdsLongArray(val ids: LongArray) {
        override fun toString() = ids.joinToString(transform = { id -> "N${id}" }, separator = ",")
    }

    data class RestaurantName(val query: String) {
        override fun toString() = "[restaurant]$query"
    }

}


