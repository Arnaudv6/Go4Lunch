package com.cleanup.go4lunch.ui.list

import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    poiRepository: PoiRepository,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    val viewStateListFlow = poiRepository.poisFromCache.map { list ->
        list.map {
            viewStateFromPoi(it)
        }
    }

    private fun viewStateFromPoi(poi: POI): PlacesListViewState {
        return PlacesListViewState(
            poi.mDescription,
            poi.mDescription,
            distanceToPoi(poi.mLocation),
            colleagues(poi.mId),
            poi.mThumbnail,
            poi.mDescription,
            likes(poi.mId)
        )
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): String {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return "???"
        val dist = geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation))
        return "${dist}m"
    }

    private fun colleagues(id: Long): Int {
        return 4
    }

    private fun likes(id: Long): Int {
        return 2
    }

}