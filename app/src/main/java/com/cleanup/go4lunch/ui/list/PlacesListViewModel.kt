package com.cleanup.go4lunch.ui.list

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ch.poole.openinghoursparser.OpeningHoursParser
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.SearchRepository
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiMapperDelegate
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.MatesByPlaceUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.leonard.OpeningHoursEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PlacesListViewModel @Inject constructor(
    matesByPlaceUseCase: MatesByPlaceUseCase,
    poiRepository: PoiRepository,
    usersRepository: UsersRepository,
    searchRepository: SearchRepository,
    gpsProviderWrapper: GpsProviderWrapper,
    private val application: Application,
    private val poiMapperDelegate: PoiMapperDelegate
) : ViewModel() {
    // using grey as R.color.colorOnSecondary don't refresh on theme change
    private val grey = ContextCompat.getColor(application, R.color.grey)

    private val orderedPoiListFlow: Flow<List<Pair<Int, PoiEntity>>> = combine(
        poiRepository.cachedPOIsListFlow,
        gpsProviderWrapper.locationFlow
    ) { list, location ->
        val locGeoPoint = GeoPoint(location)
        list.map {
            Pair(locGeoPoint.distanceToAsDouble(GeoPoint(it.latitude, it.longitude)).toInt(), it)
        }.sortedBy { it.first }
    }

    private val viewStateListFlow = combine(
        orderedPoiListFlow,
        matesByPlaceUseCase.matesByPlaceFlow,
        usersRepository.placesRatingsFlow
    ) { list, mates, ratings ->
        list.map {
            viewStateFromPoi(it.second, it.first, mates, ratings)
        }
    }

    // filtering in the end, so view feels reactive, on ViewState object, actually simpler
    val viewStateListLiveData: LiveData<List<PlacesListViewState>> = combine(
        viewStateListFlow,
        searchRepository.searchStateFlow
    ) { viewStates, terms ->
        if (terms.isNullOrEmpty()) viewStates
        else viewStates.filter {
            it.name.contains(terms, ignoreCase = true) or
                    it.mates.contains(terms, ignoreCase = true) or
                    it.address.contains(terms, ignoreCase = true)
        }
    }.asLiveData()

    private fun viewStateFromPoi(
        poi: PoiEntity,
        dist: Int,
        mates: Map<Long, ArrayList<User>>,
        ratings: Map<Long, Int>
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
            mates = "(${mates[poi.id]?.size ?: 0})",
            image = poi.imageUrl,
            hours = coloredHours.first,
            hoursColor = coloredHours.second,
            rating = ratings[poi.id]?.toFloat()
        )
    }

    // todo extraire les strings
    private fun fuzzyHours(hours: String): Pair<String, Int> {
        // https://github.com/leonardehrenfried/opening-hours-evaluator
        if (hours.isEmpty()) return Pair(application.getString(R.string.hours_unknown), grey)
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
            return Pair("Closed indefinitely", grey)
        } catch (e: Exception) {
            Log.d("PlacesListViewModel", "Failed to parse time")
        }
        return Pair(hours, grey)
    }

    private fun fuzzyInstant(instant: LocalDateTime, now: LocalDateTime): String {
        val formatted = "%d:%02d".format(instant.hour, instant.minute)
        if (instant.dayOfWeek == now.dayOfWeek) return "at $formatted"
        return "${instant.dayOfWeek.name} at $formatted"
    }
}
