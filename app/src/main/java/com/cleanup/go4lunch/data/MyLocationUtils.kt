package com.cleanup.go4lunch.data

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyLocationUtils @Inject constructor() {

    // todo Nino : make this an object?
    companion object {
        // Eiffel Tower, @5m above the WGS 84: real Paris is 35m above sea: limiting false positives
        val EIFFEL_TOWER = GeoPoint(48.8583, 2.2944, 5.0)
        val FRANCE_BOX = BoundingBox(51.404, 8.341, 42.190, -4.932)
    }
}

