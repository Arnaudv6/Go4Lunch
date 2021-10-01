package com.cleanup.go4lunch.data

import org.osmdroid.util.GeoPoint

class FranceGps {

    // todo Nino : comme ça ?
    companion object {
        // Eiffel Tower, @5m above the WGS 84: real Paris is 35m above sea: limiting false positives
        const val fallbackLatitude = 48.8583
        const val fallbackLongitude = 2.2944
        const val fallbackAltitude = 5.0
        val fallbackGeoPoint = GeoPoint(fallbackLatitude, fallbackLongitude, fallbackAltitude)

        fun inFrance(geoPoint: GeoPoint): Boolean {
            return geoPoint.latitude > 42.190 && geoPoint.latitude < 51.404 &&
                    geoPoint.longitude > -4.932 && geoPoint.longitude < 8.341
        }

        fun isDeviceLocation(geoPoint: GeoPoint): Boolean {
            return geoPoint != fallbackGeoPoint
        }

    }
}