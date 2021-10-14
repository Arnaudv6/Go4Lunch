package com.cleanup.go4lunch.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val viewActionChannel = Channel<PlacesListViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    val viewStateListFlow =
        poiRepository.poisFromCache.combine(gpsProviderWrapper.locationFlow) { list, _ ->
            list.map {
                viewStateFromPoi(it)
            }.sortedBy { viewState -> viewState.distance }
        }.onEach {
            // todo check I see this log on purpose only
            Log.e("ViewModel:", "scrolling back to top with viewAction")
            // TODO INJECT DISPATCHERS for testing
            viewModelScope.launch(Dispatchers.Main) {
                delay(200)
                viewActionChannel.trySend(PlacesListViewAction.ScrollToTop)
            }
        }

    private fun viewStateFromPoi(poi: PoiEntity): PlacesListViewState {
        val dist = distanceToPoi(GeoPoint(poi.latitude, poi.longitude))

        return PlacesListViewState(
            poi.id,
            poi.name,
            "${poi.cuisine} - ${poi.address}",
            dist,  // distance as an Int, to sort
            if (dist == null) "???" else "${dist}m",  // distance as a text, for display
            "(${usersRepository.usersGoing(poi.id).size})",
            null, // bitmap
            poi.hours,  // hours
            likes(poi.id)
        )
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): Int? {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return null
        return geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation)).toInt()
    }

    private fun likes(id: Long): Int {
        return id.toInt() // todo make this code relevant
    }

}