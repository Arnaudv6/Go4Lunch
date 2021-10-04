package com.cleanup.go4lunch.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
@Singleton
class PoiRepository @Inject constructor(
    private val poiProvider : NominatimPOIProvider
) {

    suspend fun getPoisNearGeoPoint(geoPoint: GeoPoint): List<POI> = suspendCancellableCoroutine {
        try {
            it.resume(
                poiProvider.getPOICloseTo(
                    geoPoint,
                    "restaurant",
                    30,
                    0.025
                ),
                null
            )
        } catch (e : Exception) {
            it.resumeWithException(e)
        }
    }

    // TODO ARNAUD
    suspend fun getPoisInBox(boundingBox: BoundingBox): List<POI> = suspendCancellableCoroutine {
        it.resume(
            poiProvider.getPOIInside(
                boundingBox,
                "restaurant",
                30,
            ),
            null
        )
    }

}


