package com.cleanup.go4lunch.ui.list

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.leonard.OpeningHoursEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlacesListViewModel @Inject constructor(
    private val useCase: UseCase,
    gpsProviderWrapper: GpsProviderWrapper, // todo move to usecase ?
    private val application: Application,
    private val poiMapperDelegate: PoiMapperDelegate
) : ViewModel() {
    // todo : check all colors, and those of hours in particular
    private val colorOnSecondary = ContextCompat.getColor(application, R.color.colorOnSecondary)

    // todo : en soi, là, je dépend aussi de l'heure et des collegues.
    private val viewStateListFlow: Flow<List<PlacesListViewState>> =
        combine(useCase.cachedPOIsListFlow, gpsProviderWrapper.locationFlow) { list, location ->
            list.sortedBy { poiEntity ->
                distanceBetween(  // todo remove double with line 90
                    geoPoint1 = GeoPoint(poiEntity.latitude, poiEntity.longitude),
                    geoPoint2 = GeoPoint(location)
                )
            }.map {
                viewStateFromPoi(it, location)
            }
        }

    val viewStateListLiveData = viewStateListFlow.asLiveData()

    private fun viewStateFromPoi(poi: PoiEntity, location: Location): PlacesListViewState {
        val dist = distanceBetween(
            geoPoint1 = GeoPoint(poi.latitude, poi.longitude),
            geoPoint2 = GeoPoint(location)
        )
        val coloredHours = fuzzyHours(poi.hours.orEmpty().trim())

        return PlacesListViewState(
            id = poi.id,
            name = poi.name,
            address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
            distanceText = when {
                dist > 30_000 -> "${dist / 1000}km"
                dist > 1_000 -> "${"%.1f".format(dist / 1000.0)}km"
                else -> "${dist}m"
            },
            colleagues = "(${useCase.usersGoingThere(poi.id).size})",
            image = poi.imageUrl,
            hours = coloredHours.first,
            hoursColor = coloredHours.second,
            rating = poi.rating
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
                ContextCompat.getColor(application, R.color.green)
            )
            val opens = OpeningHoursEvaluator.isOpenNext(now, rules)
            if (opens.isPresent) return Pair(
                "Closed, opens ${fuzzyInstant(opens.get(), now)}",
                ContextCompat.getColor(application, R.color.orange_darker)
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
}
