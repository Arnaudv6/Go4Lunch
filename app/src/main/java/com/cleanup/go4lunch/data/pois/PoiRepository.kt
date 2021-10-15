package com.cleanup.go4lunch.data.pois

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
        var poiListResponse: List<PoiInBoxResult>? = null
        try {
            poiListResponse = poiRetrofit.getPoiInBox(
                // or poiProvider.getPOICloseTo
                viewBox = "${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast},${boundingBox.latSouth}",
                limit = 30
            )
        } catch (e: Exception) {
            Log.e("PoiRepository", "something bad happened while requesting POIs")
            e.fillInStackTrace()
            // todo read documented exceptions
        }
        // todo Nino can it actually be null?

        // todo don't request if already in DB !
        if (poiListResponse != null) {
            for (poiResult in poiListResponse) {
                delay(1500)
                val poiEntity = poiEntityFromResult(poiResult)
                if (poiEntity != null) {
                    val poi = completePoiData(poiEntity)
                    poiDao.insertPoi(poi)
                }
                // todo snackbar
            }
        }
    }

    private suspend fun poiEntityFromResult(result: PoiInBoxResult): PoiEntity? {
        if (
            result.category != "amenity"
            || result.type != "restaurant"
            || result.osmId == null
            || result.displayName == null
            || result.lat == null
            || result.lon == null
        ) {
            return null
        }
        val poi = PoiEntity(
            result.osmId,
            result.displayName.split(",")[0],
            result.lat,
            result.lon,
            addressFromDisplayName(result.displayName),
            "", "", "", "", ""
        )
        return completePoiData(poi)
    }

    private fun addressFromDisplayName(displayName: String): String {
        val table = displayName.split(", ")
        val formattedAddress =
            "${table[1]} ${table[2]} - ${table[table.lastIndex - 1]} ${table[table.lastIndex - 5]}"
        return if (formattedAddress.trim('-', ' ')
                .isEmpty()
        ) "address unknown" else formattedAddress
    }

    private suspend fun completePoiData(poi: PoiEntity): PoiEntity {
        val result: PoiDetailResult?
        try {
            result = poiRetrofit.getPoiDetails(osmId = poi.id)
        } catch (e: Exception) {
            Log.e("PoiRepository", "something bad happened while requesting POIs")
            e.fillInStackTrace()
            // todo read documented exceptions
            return poi
        }
        val e = result?.extraTags ?: return poi
        poi.phone = e.phone ?: ""
        poi.site = e.website ?: ""
        poi.cuisine = e.cuisine?.replaceFirstChar { it.uppercaseChar() } ?: ""
        poi.hours = e.hours ?: ""
        return poi
    }

    val poisFromCache: Flow<List<PoiEntity>> = poiDao.getPoiEntities()
}

