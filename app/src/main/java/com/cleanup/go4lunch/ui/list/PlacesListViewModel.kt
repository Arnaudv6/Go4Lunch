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
import java.time.ZonedDateTime
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
        val address = poi.address.split(" - ")[0]

        return PlacesListViewState(
            poi.id,
            poi.name,
            listOfNotNull(
                poi.cuisine.ifEmpty { null },
                address.ifEmpty { null }
            ).joinToString(" - "),
            dist,  // distance as an Int, to sort
            if (dist == null) "???" else "${dist}m",  // distance as a text, for display
            "(${usersRepository.usersGoing(poi.id).size})",
            poi.imageUrl,
            fuzzyHours(poi),
            usersRepository.likes(poi.id).toFloat()
        )
    }

    companion object {
        private val WEEK_DAYS = arrayOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        private fun dayInRange(day: String): Boolean {  // this fails on ranges that go from Sunday to Monday
            return true
        }
    }

    private fun fuzzyHours(poi: PoiEntity): String {
        if (poi.hours.isEmpty()) return "hours unknown"
        // todo make this fuzzy : poi.hours
        //val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("E;H;m"))
        val now = ZonedDateTime.now()
        // hours and minutes don't have leading zeros.
        return poi.hours
        return "closed"
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): Int? {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return null
        return geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation)).toInt()
    }
}
