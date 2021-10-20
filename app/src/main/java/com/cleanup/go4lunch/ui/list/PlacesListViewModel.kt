package com.cleanup.go4lunch.ui.list

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.R
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
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val application: Application
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
        val coloredHours = fuzzyHours(poi.hours.trim())

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
            coloredHours.first,
            coloredHours.second,
            usersRepository.likes(poi.id).toFloat()
        )
    }

    private fun fuzzyHours(hours: String): Pair<String, Int> {
        // https://github.com/leonardehrenfried/opening-hours-evaluator
        if (hours.isEmpty()) return Pair(
            "hours unknown",
            ContextCompat.getColor(application, R.color.black)
        )
        try {
            val parser = OpeningHoursParser(hours.byteInputStream())
            val rules = parser.rules(true)
            val now = LocalDateTime.now()
            if (OpeningHoursEvaluator.isOpenAt(now, rules)) return Pair(
                "Opened until ${
                    fuzzyInstant(
                        OpeningHoursEvaluator.isOpenUntil(now, rules).get(),
                        now
                    )
                }",
                ContextCompat.getColor(application, R.color.green)
            )
            val opens = OpeningHoursEvaluator.isOpenNext(now, rules)
            if (opens.isPresent) return Pair(
                "Closed, opens at ${fuzzyInstant(opens.get(), now)}",
                ContextCompat.getColor(application, R.color.orange_darker)
            )
            return Pair("Closed indefinitely", ContextCompat.getColor(application, R.color.black))
        } catch (e: Exception) {
            Log.e("PlacesListViewModel", "Failed to parse time")
        }
        return Pair(hours, ContextCompat.getColor(application, R.color.black))
    }

    private fun fuzzyInstant(instant: LocalDateTime, now: LocalDateTime): String {
        val formatted = "%d:%02d".format(instant.hour, instant.minute)
        if (instant.dayOfWeek == now.dayOfWeek) return formatted
        return "${instant.dayOfWeek.name} at $formatted"
    }

    private fun distanceToPoi(geoPoint: GeoPoint?): Int? {
        if (geoPoint == null || gpsProviderWrapper.lastKnownLocation == null) return null
        return geoPoint.distanceToAsDouble(GeoPoint(gpsProviderWrapper.lastKnownLocation)).toInt()
    }
}
