package com.cleanup.go4lunch.ui.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val repo: Repository,
) : ViewModel() {

    private val mutablePOIsList = MutableStateFlow<ArrayList<POI>>(arrayListOf())
    val poisList = mutablePOIsList.asStateFlow()
    val locationAsGeoPoint = gpsProviderWrapper.locationFlow.map { loc: Location -> GeoPoint(loc) }

    init {
        gpsProviderWrapper.addLocationConsumer { location, _ ->
            run {
                Log.e("MapViewModel", "new location: $location")
                if (location != null
                    && location.latitude > 42.1900 && location.latitude < 51.404
                    && location.longitude > -4.932 && location.longitude < 8.341
                ) {
                    // todo if (location != loc)

                    CoroutineScope(Job() + Dispatchers.IO).launch {
                        val pois = repo.getPois(GeoPoint(location))
                        Log.e("MapViewModel", "received ${pois.size}: POIs")
                        mutablePOIsList.emit(pois)
                    }
                }
            }
        }
    }
}