package com.cleanup.go4lunch.data.pois

import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoiRepository @Inject constructor(
    private val poiRetrofit: PoiRetrofit,
    private val poiDao: PoiDao
) {
    // OK: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common

    val cachedPOIsListFlow: Flow<List<PoiEntity>> = poiDao.getPoiEntities()

    suspend fun getPoiById(osmId: Long): PoiEntity? = poiDao.getPoiById(osmId)

    // todo Nino ensure 1_500ms delay between any request
    suspend fun fetchPOIsInBoundingBox(boundingBox: BoundingBox): Int {
        val response = poiRetrofit.getPoiInBoundingBox(  // getPOICloseTo() also exists
            viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
            limit = 30
        )
        return response.body()?.mapNotNull { toPoiEntity(it) }
            ?.onEach { poiDao.insertPoi(it) }?.size ?: 0
    }

    suspend fun fetchPOIsInList(ids: List<Long>, refreshExisting: Boolean): Int {
        val list = if (refreshExisting) {
            ids
        } else {
            val cachedPoiIds = poiDao.getPoiIds()
            ids.filter { !cachedPoiIds.contains(it) }
        }
        // API allows to request up to 50 IDs at a time
        var number = 0
        for (ids_chunk in list.chunked(50)) {
            if (ids_chunk.isNotEmpty()) {
                val response = poiRetrofit.getPOIsInList(
                    idsLongArray = PoiRetrofit.IdsLongArray(ids_chunk.toLongArray())
                )
                number += response.body()?.mapNotNull { toPoiEntity(it) }
                    ?.onEach { poiDao.insertPoi(it) }?.size ?: 0
            }
        }
        return number
    }

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
            id = response.osmId,
            name = response.address.amenity,
            latitude = response.lat,
            longitude = response.lon,
            address = toFuzzyAddress(response.address),
            cuisine = response.extraTags?.cuisine?.replaceFirstChar { it.uppercaseChar() } ?: "",
            imageUrl = PoiImages.getImageUrl(),
            phone = response.extraTags?.phone,
            site = response.extraTags?.website,
            hours = response.extraTags?.hours,
            rating = null
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

    suspend fun updatePoiRating(osmId: Long, rating: Int) =
        poiDao.updatePoiRating(osmId, rating)

}

