package com.cleanup.go4lunch.repository

import android.location.Location
import android.util.Log
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.data.GpsProviderWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor() : GpsProviderWrapper.OnLocationChangedListener {

    private val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)

    // todo do not pass complete repo object here.

    init {
        GpsProviderWrapper.setLocationUpdateMinDistance(10F)  // float, meters
        GpsProviderWrapper.locationUpdateMinTime = 5000 // long, milliseconds // TODO arnaud
    }

    // TODO A simplifier ? Plus besoin vu qu'on a le flow du GpsProviderWrapper ? A voir
    override fun onLocationChanged(location: Location?) {
        updateLocation(location)
    }

    // TODO A bouger dans le VM
    private fun updateLocation(location: Location?) {
        if (location != null
            && location.latitude < 51.404 && location.latitude > 42.190
            && location.longitude < 8.341 && location.longitude > -4.932
        ) {
            locationFlow.value = location
            Log.e("Repository", "setLocation() called with: $location")

            if (location != loc) {
                CoroutineScope(Job() + Dispatchers.IO).launch {
                    getPois(location)
                }
            }
        }
    }

    suspend fun getPois(location: Location) : List<POI> = suspendCancellableCoroutine {
        poiProvider.getPOICloseTo(
            GeoPoint(location),
            "restaurant",
            50,
            0.025
        )
    }
}


