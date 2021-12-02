package com.cleanup.go4lunch.ui.list

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.MatesByPlaceUseCase
import com.cleanup.go4lunch.data.useCase.RatedPOIsUseCase
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.leonard.OpeningHoursEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlacesListViewModel @Inject constructor(
    matesByPlaceUseCase: MatesByPlaceUseCase,
    ratedPOIsUseCase: RatedPOIsUseCase,
    poiRepository: PoiRepository,
    gpsProviderWrapper: GpsProviderWrapper,
    private val application: Application,
    private val poiMapperDelegate: PoiMapperDelegate
) : ViewModel() {
    // using grey as R.color.colorOnSecondary don't refresh on theme change
    private val colorOnSecondary = ContextCompat.getColor(application, R.color.grey)

    private val orderedPoiListFlow: Flow<List<Pair<Int, PoiEntity>>> = combine(
        poiRepository.cachedPOIsListFlow,
        gpsProviderWrapper.locationFlow
    ) { list, location ->
        val locGeoPoint = GeoPoint(location)
        list.map {
            Pair(locGeoPoint.distanceToAsDouble(GeoPoint(it.latitude, it.longitude)).toInt(), it)
        }.sortedBy { it.first }
    }

    val viewStateListLiveData: LiveData<List<PlacesListViewState>> = combine(
        orderedPoiListFlow,
        matesByPlaceUseCase.matesByPlaceFlow,
        ratedPOIsUseCase.placesIdRatingsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HashMap(0)
        )
    ) { list, mates, ratings ->
        list.map {
            viewStateFromPoi(it.second, it.first, mates, ratings)
        }
    }.asLiveData()

    private fun viewStateFromPoi(
        poi: PoiEntity,
        dist: Int,
        mates: HashMap<Long, ArrayList<String>>,
        ratings: HashMap<Long, Int>
    ): PlacesListViewState {
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
            colleagues = "(${mates[poi.id]?.size ?: 0})",
            image = poi.imageUrl,
            hours = coloredHours.first,
            hoursColor = coloredHours.second,
            rating = ratings[poi.id]?.toFloat()
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
}
