package com.cleanup.go4lunch.repository

import com.cleanup.go4lunch.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor() {

    private val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)

    // Todo Nino : comme ça ?
    suspend fun getPois(geoPoint: GeoPoint): ArrayList<POI> = suspendCancellableCoroutine {
        it.resume(
            poiProvider.getPOICloseTo(
                geoPoint,
                "restaurant",
                50,
                0.025
            ),
            null
        )
    }
}


