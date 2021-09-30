package com.cleanup.go4lunch.data

import android.location.Location
import com.cleanup.go4lunch.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsProviderWrapper @Inject constructor() : IMyLocationConsumer {

    private val provider = GpsMyLocationProvider(MainApplication.instance)
    private val listeners = mutableSetOf<OnLocationChangedListener>()

    private val mutableLocationFlow = MutableStateFlow(
        Location("repository").apply {
            latitude = 48.8583  // starting Location: Eiffel Tower
            longitude = 2.2944
        }
    )
    val locationFlow = mutableLocationFlow.asStateFlow()

    init {
        provider.startLocationProvider(this)
    }

    fun setLocationUpdateMinDistance(distance : Float) {
        provider.locationUpdateMinDistance = distance
    }

    fun addOnLocationChangedListener(listener: OnLocationChangedListener) {
        listeners.add(listener)
    }

    fun removeOnLocationChangedListener(listener: OnLocationChangedListener) {
        listeners.remove(listener)
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        location?.let {
            mutableLocationFlow.value = it
        }

        listeners.forEach {
            it.onLocationChanged(location)
        }
    }

    interface OnLocationChangedListener {
        fun onLocationChanged(location: Location?)
    }
}

