package com.cleanup.go4lunch.ui.map

import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox

sealed class MapViewAction {
    data class CenterOnMe(val geoPoint: IGeoPoint) : MapViewAction()
    data class InitialBox(val boundingBox: BoundingBox) : MapViewAction()
    data class PoiRetrieval(val progress: Pair<Int, Int>) : MapViewAction()
}
