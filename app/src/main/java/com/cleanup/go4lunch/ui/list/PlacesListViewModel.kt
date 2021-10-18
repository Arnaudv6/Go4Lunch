package com.cleanup.go4lunch.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.leonard.OpeningHoursEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
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
            // todo Nino: onEach, c'est une bonne idée ?
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
            fuzzyHours(poi.hours),
            usersRepository.likes(poi.id).toFloat()
        )
    }

    private fun fuzzyHours(hours: String): String {
        // https://github.com/leonardehrenfried/opening-hours-evaluator
        if (hours.isEmpty()) return "hours unknown"

        try {
            val parser = OpeningHoursParser(hours.byteInputStream())
            val rules = parser.rules(true)
            val now = LocalDateTime.now()
            if (OpeningHoursEvaluator.isOpenAt(now, rules))
                return "Open" // todo: until when ?
            val next = OpeningHoursEvaluator.isOpenNext(now, rules)
            if (next.isPresent) {
                if (next.get().dayOfWeek == now.dayOfWeek) return "opens at ${next.get().hour}:${next.get().minute.toString().padStart(1,'0')}"
                return "opens on ${next.get().dayOfWeek.name} at ${next.get().hour}:${next.get().minute.toString().padStart(2,'0')}"
            }
        } catch (e :Exception){
            Log.e("PlacesListViewModel", "Failed to parse time")
        }
        return hours
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): Int? {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return null
        return geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation)).toInt()
    }
}
