package com.cleanup.go4lunch.repository

import android.location.Location
import android.util.Log
import androidx.annotation.WorkerThread
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.MainApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor() {

    private val loc = Location("repository")
    private val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)
    private val locationFlow = MutableStateFlow(loc)
    private val pointsOfInterest = MutableStateFlow<ArrayList<POI>>(arrayListOf())

    private val gps = GpsMyLocationProvider(MainApplication.instance)

    init {
        gps.locationUpdateMinDistance = 10F  // float, meters
        gps.locationUpdateMinTime = 5000 // long, milliseconds
        gps.startLocationProvider { location, _ -> updateLocation(location) }

        loc.latitude = 48.8583  // starting Location: Eiffel Tower
        loc.longitude = 2.2944
        locationFlow.value = loc
    }

    private fun updateLocation(location: Location) {
        if (location.latitude < 51.404 && location.latitude > 42.190
            && location.longitude < 8.341 && location.longitude > -4.932
        ) {
            locationFlow.value = location
            Log.e("Repository", "setLocation() called with: $location")

            if (location != loc) {
                CoroutineScope(Job() + Dispatchers.IO).launch {
                    requestPOIs(location)
                }
            }
        }
    }

    @WorkerThread
    suspend fun requestPOIs(location: Location) {
        val pointsOfInterest = poiProvider.getPOICloseTo(
            GeoPoint(location),
            "restaurant",
            50,
            0.025
        )
        for (poi in pointsOfInterest){
            Log.e("repo POIs response", poi.toString() )
        }
        this.pointsOfInterest.value = pointsOfInterest
        // or emit()?
        //todo : make a try: catch on this web request....
    }

    fun getLocationFlow(): Flow<Location> {
        return locationFlow
    }

    fun getPointsOfInterest(): Flow<ArrayList<POI>> {
        return pointsOfInterest
    }
}


