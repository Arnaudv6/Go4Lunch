package com.cleanup.go4lunch.ui

import javax.inject.Inject
import javax.inject.Singleton

// this function belongs neither in
//  Entity (which must be a POJO)
//  Util class (which would be static = non testable/injectable)
//  Object "class" as (not testable/injectable)
// so we une a singleton (for static) delegate (for injection).
@Singleton
class PoiMapperDelegate @Inject constructor() {
    fun cuisineAndAddress(
        cuisine: String,
        address: String
    ): String = listOfNotNull(
        cuisine.ifEmpty { null },
        address.split(" - ")[0].ifEmpty { null }
    ).joinToString(" - ")
}
