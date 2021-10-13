package com.cleanup.go4lunch.data.pois

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class PoiRepository @Inject constructor(
    private val poiProvider: NominatimPOIProvider,
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
                poiFromResult(it)
            })
        } catch (e: Exception) {
            Log.e("POI repository", "something bad happened while requesting POIs")
            e.fillInStackTrace()
            // todo read documented exceptions
        }
    }

    private fun poiFromResult(result: PoiInBoxResult): POI? {
        if (result.osmId == null
            || result.displayName == null
            || result.lat == null
            || result.lon == null
            || result.placeId == null
        ) return null
        val poi = POI(444)
        poi.mId = result.placeId
        poi.mLocation = GeoPoint(result.lat, result.lon)
        poi.mDescription = result.displayName
        poi.mType = "restaurant"
        poi.mCategory = "coucou"
        return poi
    }


    suspend fun getPOIsInBox2(boundingBox: BoundingBox) {
        try {
            val poiList = poiProvider.getPOIInside(
                // or poiProvider.getPOICloseTo
                boundingBox,
                "restaurant",
                30,
            )
            // todo can it actually be null?
            if (poiList != null) putPoiListInCache(poiList)
        } catch (e: Exception) {
            Log.e("POI repository", "something bad happened while requesting POIs")
            // todo read documented exceptions
        }
    }


    val poisFromCache: Flow<List<POI>> = poiDao.getPoiEntities().map { poiEntitiesList ->
        poiEntitiesList.map {
            poiFromPoiEntity(it)
        }
    }

    private companion object {
        private const val POI_SERVICE_GO_4_LUNCH: Int = 4
        private fun poiFromPoiEntity(poiEntity: PoiEntity): POI {
            val poi = POI(POI_SERVICE_GO_4_LUNCH)
            poi.mId = poiEntity.id
            poi.mCategory = poiEntity.category
            poi.mDescription = poiEntity.description
            poi.mLocation = GeoPoint(poiEntity.latitude, poiEntity.longitude)
            return poi
        }
    }

    private suspend fun putPoiListInCache(poiList: List<POI>?) {
        if (poiList != null) {
            for (poi in poiList) {
                poiDao.insertPoi(PoiEntity(poi))
            }
        }
    }
}

