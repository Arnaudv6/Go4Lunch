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
    // OK: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common

    val cachedPOIsListFlow: Flow<List<PoiEntity>> = poiDao.getPoiEntities()

    suspend fun getPoiById(osmId: Long): PoiEntity? {
        return poiDao.getPoiById(osmId)
    }

    // todo ensure 1_500ms delay
    suspend fun fetchPOIsInBox(boundingBox: BoundingBox): Int =
        try {
            poiRetrofit.getPoiInBox(  // getPOICloseTo() also exists
                viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
                limit = 30
            )
        } catch (e: Exception) {  // todo read documented exceptions
            Log.e("PoiRepository", "something bad happened while requesting POIs")
            e.printStackTrace()
            emptyList()
        }.mapNotNull {
            toPoiEntity(it)
        }.onEach {
            poiDao.insertPoi(it)
        }.size

    private fun toPoiEntity(response: PoiInBoxResponse): PoiEntity? = if (
        response.category != "amenity"
        || response.type != "restaurant"
        || response.osmId == null
        || response.address == null
        || response.address.amenity == null
        || response.lat == null
        || response.lon == null
    ) {
        null
    } else {
        PoiEntity(
            response.osmId,
            response.address.amenity,
            response.lat,
            response.lon,
            toFuzzyAddress(response.address),
            response.extraTags?.cuisine?.replaceFirstChar { it.uppercaseChar() } ?: "",
            PoiImages.getImageUrl(),
            response.extraTags?.phone,
            response.extraTags?.website ?: "",
            response.extraTags?.hours ?: ""
        )
    }

    private fun toFuzzyAddress(address: PoiInBoxResponse.Address): String =
        if ((address.number == null && address.road == null)
            || (address.postcode == null && address.municipality == null)
        ) {
            "address unknown"
        } else {
            "${address.number.orEmpty()} ${address.road} - ${address.postcode} ${address.municipality}".trim()
        }
}

