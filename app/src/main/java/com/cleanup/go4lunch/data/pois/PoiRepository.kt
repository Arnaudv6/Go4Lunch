package com.cleanup.go4lunch.data.pois

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class PoiRepository @Inject constructor(
    private val poiRetrofit: PoiRetrofit,
    private val poiDao: PoiDao
) {
    // note: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common: OK!

    suspend fun getPOIsInBox(boundingBox: BoundingBox) {
        var poiListResponse : List<PoiInBoxResult>? = null
        try {
            poiListResponse = poiRetrofit.getPoiInBox(
                // or poiProvider.getPOICloseTo
                viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
                limit = 30
            )
        } catch (e: Exception) {
            Log.e("POI repository", "something bad happened while requesting POIs")
            e.fillInStackTrace()
            // todo read documented exceptions
        }
        // todo Nino can it actually be null?
        if (poiListResponse != null) {
            for (poi in poiListResponse.mapNotNull { poiEntityFromResult(it) }) {
                poiDao.insertPoi(poi)
            }
        }
    }

    private fun poiEntityFromResult(result: PoiInBoxResult): PoiEntity? {
        if (
            result.category != "amenity"
            || result.type != "restaurant"
            || result.placeId == null
            || result.displayName == null
            || result.lat == null
            || result.lon == null
        )
        {
            return null
        }
        val poi = PoiEntity(
            result.placeId,
            result.displayName.split(",")[0],
            result.lat,
            result.lon,
            "", "", "", "", ""
        )
        return completePoiData(poi)
    }

    private fun completePoiData(poi: PoiEntity): PoiEntity {
        return poi
    }

    val poisFromCache: Flow<List<PoiEntity>> = poiDao.getPoiEntities()
}

