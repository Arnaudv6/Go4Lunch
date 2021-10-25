package com.cleanup.go4lunch.data

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyLocationUtils @Inject constructor() {

    companion object {
        // Eiffel Tower, @5m above the WGS 84: real Paris is 35m above sea: limiting false positives
        const val fallbackLatitude = 48.8583
        const val fallbackLongitude = 2.2944
        const val fallbackAltitude = 5.0
        val FRANCE_BOX : BoundingBox = BoundingBox(51.404, 8.341, 42.190, -4.932)
        val fallbackGeoPoint = GeoPoint(fallbackLatitude, fallbackLongitude, fallbackAltitude)
    }

}

