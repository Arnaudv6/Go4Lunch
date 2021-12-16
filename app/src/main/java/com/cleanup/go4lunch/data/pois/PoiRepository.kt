package com.cleanup.go4lunch.data.pois

import android.app.Application
import android.util.Log
import com.cleanup.go4lunch.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoiRepository @Inject constructor(
    private val application: Application,
    private val poiRetrofit: PoiRetrofit,
    private val poiDao: PoiDao
) {
    // OK: 1 repo for 2 sources (PoiDao and OsmDroidBonusPack functions), with POIs in common

    val cachedPOIsListFlow: Flow<List<PoiEntity>> = poiDao.getPoiEntities()
    private val nominatimMutexChannel = Channel<Long>(capacity = 1).apply { trySend(0) }

    suspend fun getPoiById(osmId: Long): PoiEntity? = poiDao.getPoiById(osmId)

    // this fun can be moved to utils class and injected
    private suspend inline fun <reified T> ensureGentleRequests(request: () -> T): T {
        val previousEpoch = nominatimMutexChannel.receive()
        val requestDelay = (1_500 - (System.currentTimeMillis() - previousEpoch))
        if (requestDelay > 0) Log.d(
            this.javaClass.canonicalName,
            application.getString(R.string.log_request_delay).format(requestDelay)
        )
        delay(requestDelay)  // negative delays ignored no need to coerceAtLeast(0)

        val response = request.invoke()

        // todo pour les tests, injecter la Clock
        nominatimMutexChannel.trySend(System.currentTimeMillis())
        return response
    }

    suspend fun fetchPOIsInBoundingBox(box: BoundingBox): Int {
        val response = ensureGentleRequests {
            poiRetrofit.getPoiInBoundingBox(  // getPOICloseTo() also exists
                viewBox = "${box.lonWest},${box.latNorth},${box.lonEast},${box.latSouth}",
                limit = 30
            )
        }

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
                val response = ensureGentleRequests {
                    poiRetrofit.getPOIsInList(
                        idsLongArray = PoiRetrofit.IdsLongArray(ids_chunk.toLongArray())
                    )
                }
                number += response.body()?.mapNotNull { toPoiEntity(it) }
                    ?.onEach { poiDao.insertPoi(it) }?.size ?: 0
            }
        }
        return number
    }

    private fun toPoiEntity(response: PoiResponse): PoiEntity? = if (
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

    private fun toFuzzyAddress(address: PoiResponse.Address): String =
        if ((address.number == null && address.road == null)
            || (address.postcode == null && address.municipality == null)
        ) application.getString(R.string.address_unknown)
        else "${address.number.orEmpty()} ${address.road} - ${address.postcode} ${address.municipality}".trim()

    suspend fun clearCache() = poiDao.nukePOIS()

}

