package com.freemyip.go4lunch.ui.map

import androidx.annotation.DrawableRes
import org.osmdroid.util.GeoPoint

data class MapViewState(
    val pinList: List<Pin>
    // can't make it a list of Marker as constructor needs a map.
) {
    data class Pin(
        val id: Long,
        val name: String,
        val mates: String,
        @DrawableRes val icon: Int,
        val location: GeoPoint
    )
}


