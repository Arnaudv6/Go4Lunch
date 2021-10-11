package com.cleanup.go4lunch.ui.map

import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.BoundingBox

data class MapViewState(
    val boundingBox : BoundingBox,
    val pois : List<POI>
)
