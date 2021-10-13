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
        try {
            val poiListResponse = poiRetrofit.getPoiInBox(
                // or poiProvider.getPOICloseTo
                viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
                limit = 30
            )
            // todo can it actually be null?
            putPoiListInCache(poiListResponse.mapNotNull {
                if (
                    it.placeId == null
                    || it.lat == null
                    || it.lon == null
                    || it.displayName == null
                ) null else
                    PoiEntity(
                        it.placeId,
                        it.lat,
                        it.lon,
                        "type",
                        "category",
                        it.displayName
                    )
            })
        } catch (e: Exception) {
            Log.e("POI repository", "something bad happened while requesting POIs")
            e.fillInStackTrace()
            // todo read documented exceptions
        }
    }

    val poisFromCache: Flow<List<PoiEntity>> = poiDao.getPoiEntities()

    private suspend fun putPoiListInCache(poiList: List<PoiEntity>?) {
        if (poiList != null) {
            for (poi in poiList) {
                poiDao.insertPoi(poi)
            }
        }
    }
}

