package com.cleanup.go4lunch.ui.map

import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint

data class MapViewState(
    val pinList: List<Pin>
    // can't make it a list of Marker as constructor needs a map.
) {
    data class Pin(
        val id: Long,
        val name: String,
        val colleagues: String,
        val icon: Drawable?,
        val location: GeoPoint
    )
}


