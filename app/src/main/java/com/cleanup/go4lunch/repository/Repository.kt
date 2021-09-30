package com.cleanup.go4lunch.repository

import android.location.Location
import android.util.Log
import androidx.annotation.WorkerThread
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.MainApplication
import com.cleanup.go4lunch.data.DualConsumerGps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor() : DualConsumerGps.RepositoryConsumer {

    private val loc = Location("repository")
    private val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)
    private val locationFlow = MutableStateFlow(loc)
    private val pointsOfInterest = MutableStateFlow<ArrayList<POI>>(arrayListOf())

    // todo do not pass complete repo object here.
    val gps = DualConsumerGps(MainApplication.instance, this)

    init {
        gps.locationUpdateMinDistance = 10F  // float, meters
        gps.locationUpdateMinTime = 5000 // long, milliseconds

        loc.latitude = 48.8583  // starting Location: Eiffel Tower
        loc.longitude = 2.2944
        locationFlow.value = loc
    }

    override fun executeAlso(location: Location?) {
        updateLocation(location)
    }

    private fun updateLocation(location: Location?) {
        if (location != null
            && location.latitude < 51.404 && location.latitude > 42.190
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

    // todo Nino : WorkerThread or suspend ?
    @WorkerThread
    suspend fun requestPOIs(location: Location) {
        val pointsOfInterest = poiProvider.getPOICloseTo(
            GeoPoint(location),
            "restaurant",
            50,
            0.025
        )
        Log.e("Repository", "requestPOIs() received ${pointsOfInterest.size} POIs")
        this.pointsOfInterest.value = pointsOfInterest
        // todo Nino pas emit(), on est d'accord?
        //todo : make a try: catch on this web request....
    }

    fun getLocationFlow(): Flow<Location> {
        return locationFlow
    }

    fun getPointsOfInterest(): Flow<ArrayList<POI>> {
        return pointsOfInterest
    }
}


