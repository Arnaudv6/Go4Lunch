package com.cleanup.go4lunch.ui

import javax.inject.Inject
import javax.inject.Singleton

// TODO A utiliser dans l'autre VM
@Singleton
class PoiMapperDelegate @Inject constructor() {
    fun cuisineAndAddress(
        address: String,
        cuisine: String
    ): String = listOfNotNull(
        cuisine.ifEmpty { null },
        address.split(" - ")[0].ifEmpty { null }
    ).joinToString(" - ")
}
