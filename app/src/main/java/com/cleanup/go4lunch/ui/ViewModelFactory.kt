package com.cleanup.go4lunch.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cleanup.go4lunch.ui.map.MapViewModel
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

// todo Nino : ça, tu faisais comment avant hilt?
/*
class ViewModelFactory private constructor() : ViewModelProvider.Factory {

    val context = Context
    val gps = GpsMyLocationProvider(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(gps) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        val INSTANCE = ViewModelFactory()
    }
}
*/

