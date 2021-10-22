package com.cleanup.go4lunch.data.pois

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class PoiRepository @Inject constructor(
    private val poiRetrofit: PoiRetrofit,
    private val poiDao: PoiDao
) {
    // OK: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common

    val poiDataRetrievalStateFlow: MutableStateFlow<Pair<Int, Int>> = MutableStateFlow(Pair(0, 0))

    // todo
    //  call / call.enqueue?
    //  SnackBar if no result
    //  CONSIDER REFACTORING THIS INTO A FLOW EMITTING PoiEntity
    //  fun getPOIsInBox(boundingBox: BoundingBox) = flow {
    suspend fun getPOIsInBox(boundingBox: BoundingBox) {
        val poiListResponse: List<PoiInBoxResult>? = try {
            poiRetrofit.getPoiInBox(
                // or poiProvider.getPOICloseTo
                viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
                limit = 30
            )
        } catch (e: Exception) {  // todo read documented exceptions
            Log.e("PoiRepository", "something bad happened while requesting POIs")
            e.printStackTrace()
            null
        }
        // todo
        //  delay(1500)
        //  don't request if already in DB ?
        if (poiListResponse != null) {  // yes, it can be null
            for (poiResultIdx in 0..poiListResponse.lastIndex) {
                val poiEntity = poiEntityFromResult(poiListResponse[poiResultIdx])
                if (poiEntity != null) {
                    poiDao.insertPoi(poiEntity)
                }
                poiDataRetrievalStateFlow.emit(Pair(poiResultIdx + 1, poiListResponse.size))
            }
        }
    }

    private fun poiEntityFromResult(result: PoiInBoxResult): PoiEntity? {
        if (
            result.category != "amenity"
            || result.type != "restaurant"
            || result.osmId == null
            || result.address == null
            || result.address.amenity == null
            || result.lat == null
            || result.lon == null
        ) return null
        return PoiEntity(
            result.osmId,
            result.address.amenity,
            result.lat,
            result.lon,
            addressFromDisplayName(result.address),
            result.extraTags?.cuisine?.replaceFirstChar { it.uppercaseChar() } ?: "",
            PoiImages.getImageUrl(),
            result.extraTags?.phone ?: "",
            result.extraTags?.website ?: "",
            result.extraTags?.hours ?: ""
        )
    }

    private fun addressFromDisplayName(address: PoiInBoxResult.Address): String {
        if ((address.number == null && address.road == null)
            || (address.postcode == null && address.municipality == null)
        ) return "address unknown"
        return "${address.number} ${address.road} - ${address.postcode} ${address.municipality}"
    }

    val poisFromCache: Flow<List<PoiEntity>> = poiDao.getPoiEntities()
}

