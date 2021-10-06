package com.cleanup.go4lunch.ui.map

import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox

sealed class MapViewAction {
    data class CenterOnMe(val geoPoint: IGeoPoint) : MapViewAction()
}
