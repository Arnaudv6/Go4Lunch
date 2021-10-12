package com.cleanup.go4lunch.data.pois

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val poiDao: PoiDao
) {
    // note: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common: OK!
    suspend fun getPOIsInBox(boundingBox: BoundingBox): List<POI>? {
        val poiList: List<POI>?
        try {
            poiList = poiProvider.getPOIInside(
                // poiProvider.getPOICloseTo
                boundingBox,
                "restaurant",
                30,
            )
        } catch (e: Exception) {
            // todo read documented exceptions
            return null
        }
        // todo if (poiList != null) {
        CoroutineScope(Dispatchers.IO).launch {
            putPOIsInCache(poiList)
        }
        return poiList
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
            poi.mType = poiEntity.type
            return poi
        }
    }

    suspend fun putPOIsInCache(poiList: List<POI>) {
        for (poi in poiList) {
            poiDao.insertPoi(PoiEntity(poi))
        }
    }


}


