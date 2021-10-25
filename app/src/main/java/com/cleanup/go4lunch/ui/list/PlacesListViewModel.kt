package com.cleanup.go4lunch.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.leonard.OpeningHoursEvaluator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
// todo Nino, can silence this warning, right?
@SuppressLint("StaticFieldLeak")
class PlacesListViewModel @Inject constructor(
    poiRepository: PoiRepository,
    gpsProviderWrapper: GpsProviderWrapper,
    private val usersRepository: UsersRepository,
    ioDispatcher: CoroutineDispatcher,
    @ApplicationContext val appContext: Context
) : ViewModel() {
    val viewActionLiveData = SingleLiveEvent<PlacesListViewAction>()
    // This would have been a channel, in pure kotlin/flow terms, but livedata fits better the view.

    // todo Nino : what's the right way (this fails and uses default value)
    private val colorOnSecondary =
        MaterialColors.getColor(appContext, R.attr.colorOnSecondary, Color.parseColor("#888888"))

    private val recyclerViewStabilizedMutableSharedFlow = MutableSharedFlow<Unit>(replay = 1)

    private val viewStateListFlow: Flow<List<PlacesListViewState>> =
        poiRepository.poisFromCache.combine(gpsProviderWrapper.locationFlow) { list, location ->
            list.sortedBy { poiEntity ->
                distanceBetween(  // todo remove double with line 86
                    geoPoint1 = GeoPoint(poiEntity.latitude, poiEntity.longitude),
                    geoPoint2 = GeoPoint(location)
                )
            }.map {
                viewStateFromPoi(it, location)
            }
        }

    val viewStateListLiveData = viewStateListFlow.asLiveData()

    init {
        viewModelScope.launch(ioDispatcher) {
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
                viewActionLiveData.value = PlacesListViewAction.ScrollToTop
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
            distanceText = when {
                dist > 30_000 -> "${dist / 1000}km"
                dist > 1_000 -> "${"%.1f".format(dist / 1000.0)}km"
                else -> "${dist}m"
            },
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
            colorOnSecondary
        )
        try {
            val parser = OpeningHoursParser(hours.byteInputStream())
            val rules = parser.rules(true)
            val now = LocalDateTime.now()
            if (OpeningHoursEvaluator.isOpenAt(now, rules)) return Pair(
                "Open (closes ${
                    fuzzyInstant(OpeningHoursEvaluator.isOpenUntil(now, rules).get(), now)
                })",
                ContextCompat.getColor(appContext, R.color.green)
            )
            val opens = OpeningHoursEvaluator.isOpenNext(now, rules)
            if (opens.isPresent) return Pair(
                "Closed, opens ${fuzzyInstant(opens.get(), now)}",
                ContextCompat.getColor(appContext, R.color.orange_darker)
            )
            return Pair(
                "Closed indefinitely",
                colorOnSecondary
            )
        } catch (e: Exception) {
            Log.d("PlacesListViewModel", "Failed to parse time")
        }
        return Pair(hours, colorOnSecondary)
    }

    private fun fuzzyInstant(instant: LocalDateTime, now: LocalDateTime): String {
        val formatted = "%d:%02d".format(instant.hour, instant.minute)
        if (instant.dayOfWeek == now.dayOfWeek) return "at $formatted"
        return "${instant.dayOfWeek.name} at $formatted"
    }

    private fun distanceBetween(geoPoint1: GeoPoint, geoPoint2: GeoPoint): Int =
        geoPoint1.distanceToAsDouble(geoPoint2).toInt()

    fun onRecyclerViewIdle() {
        recyclerViewStabilizedMutableSharedFlow.tryEmit(Unit)
    }
}
