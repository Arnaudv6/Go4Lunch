package com.cleanup.go4lunch.ui.list

import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    poiRepository: PoiRepository,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    val viewStateListFlow =
        poiRepository.poisFromCache.combine(gpsProviderWrapper.locationFlow) { list, _ ->
            list.map {
                viewStateFromPoi(it)
            }.sortedBy { viewState -> viewState.distance }
        }

    private fun viewStateFromPoi(poi: POI): PlacesListViewState {
        return PlacesListViewState(
            poi.mId,
            poi.mDescription,
            poi.mDescription,
            distanceToPoi(poi.mLocation),
            colleagues(poi.mId),
            poi.mThumbnail,
            poi.mDescription,
            likes(poi.mId)
        )
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): Int? {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return null
        return geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation)).toInt()
    }

    private fun colleagues(id: Long): Int {
        return id.toInt() // todo make this code relevant
    }

    private fun likes(id: Long): Int {
        return id.toInt() // todo make this code relevant
    }

}