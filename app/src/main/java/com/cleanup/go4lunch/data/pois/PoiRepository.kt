package com.cleanup.go4lunch.data.pois

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
@Singleton
class PoiRepository @Inject constructor(
    private val poiProvider: NominatimPOIProvider,
    private val poiDao: PoiDao
) {
    // todo Nino : là, je tape dans 2 sources:
    //  PoiDao
    //  les fonctions de OsmDroidBonusPack
    //  le point commun, c'est les POIs. je garde qu'un repo?
    suspend fun getPOIsNearGeoPoint(geoPoint: GeoPoint): List<POI>? = suspendCancellableCoroutine {
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
        } catch (e: Exception) {
            it.resumeWithException(e)
        }
    }

    suspend fun getPOIsInBox(boundingBox: BoundingBox): List<POI>? = suspendCancellableCoroutine {
        try {
            it.resume(
                poiProvider.getPOIInside(
                    boundingBox,
                    "restaurant",
                    30,
                ),
                null
            )
        } catch (e: Exception) {
            it.resumeWithException(e)
        }
    }

    val poisFromCache: Flow<List<POI>> = poiDao.getPoiEntities().map { poiEntitiesList ->
        poiEntitiesList.map {
            poiFromPoiEntity(it)
        }
    }

    companion object{
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

    fun putPOIsInCache(poiList: List<POI>) {
        CoroutineScope(Dispatchers.IO).launch {
            for (poi in poiList) {
                poiDao.insertPoi(PoiEntity(poi))
            }
        }
    }


}


