package com.cleanup.go4lunch.ui.map

import com.cleanup.go4lunch.data.pois.PoiEntity
import org.osmdroid.util.BoundingBox

data class MapViewState(
    val boundingBox: BoundingBox,
    val poiList: List<PoiEntity>
)
