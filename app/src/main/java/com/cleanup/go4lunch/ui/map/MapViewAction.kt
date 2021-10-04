package com.cleanup.go4lunch.ui.map

import org.osmdroid.api.IGeoPoint

sealed class MapViewAction {
    data class Zoom(
        val geoPoint: IGeoPoint,
        val zoom: Double,
        val speed: Long
    ) : MapViewAction()

    // TODO ARNAUD any other view action goes here
}
