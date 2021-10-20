package com.cleanup.go4lunch.ui.list

import android.app.Application
import android.location.Location
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PlacesListViewModel @Inject constructor(
    poiRepository: PoiRepository,
    gpsProviderWrapper: GpsProviderWrapper,
    private val usersRepository: UsersRepository,
    private val application: Application
) : ViewModel() {

    private val viewActionChannel = Channel<PlacesListViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    private val recyclerViewStabilizedMutableSharedFlow = MutableSharedFlow<Unit>(replay = 1)

    val viewStateListFlow: Flow<List<PlacesListViewState>> =
        poiRepository.poisFromCache.combine(gpsProviderWrapper.locationFlow) { list, location ->
            list.sortedBy { poiEntity ->
                distanceBetween(
                    geoPoint1 = GeoPoint(poiEntity.latitude, poiEntity.longitude),
                    geoPoint2 = GeoPoint(location)
                )
            }.map {
                viewStateFromPoi(it, location)
            }
        }

    init {
        // TODO INJECT DISPATCHERS
        viewModelScope.launch() {
            combine(
                recyclerViewStabilizedMutableSharedFlow.sample(1_000).onEach {
                    Log.d(
                        "Nino",
                        "recyclerViewStabilizedMutableSharedFlow.sample() called"
                    )
                },
                viewStateListFlow.debounce(200).onEach {
                    Log.d(
                        "Nino",
                        "newPoiExposedMutableSharedFlow.debounce() called"
                    )
                }
            ) { _, _ ->
                Log.d("Nino", "combine() called, emitting scrollToTop")
                viewActionChannel.trySend(PlacesListViewAction.ScrollToTop)
            }
        }
    }

    private fun viewStateFromPoi(poi: PoiEntity, location: Location): PlacesListViewState {
        val dist = distanceBetween(
            geoPoint1 = GeoPoint(poi.latitude, poi.longitude),
            geoPoint2 = GeoPoint(location)
        )
        val address = poi.address.split(" - ")[0]
        val coloredHours = fuzzyHours(poi.hours.trim())

        return PlacesListViewState(
            id = poi.id,
            name = poi.name,
            address = listOfNotNull(
                poi.cuisine.ifEmpty { null },
                address.ifEmpty { null }
            ).joinToString(" - "),
            distanceText = "${dist}m",  // distance as a text, for display
            colleagues = "(${usersRepository.usersGoing(poi.id).size})",
            image = poi.imageUrl,
            hours = coloredHours.first,
            hoursColor = coloredHours.second,
            likes = usersRepository.likes(poi.id).toFloat()
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

    private fun distanceBetween(geoPoint1: GeoPoint, geoPoint2: GeoPoint): Int =
        geoPoint1.distanceToAsDouble(geoPoint2).toInt()

    fun onRecyclerViewIdle() {
        recyclerViewStabilizedMutableSharedFlow.tryEmit(Unit)
    }
}
