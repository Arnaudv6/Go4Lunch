package com.cleanup.go4lunch.ui.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    // todo Nino : I don't understand this error.
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val repo: Repository,
) : ViewModel() {

    private val mutablePOIsList = MutableStateFlow<ArrayList<POI>>(arrayListOf())
    val poisList = mutablePOIsList.asStateFlow()
    val locationAsGeoPoint = gpsProviderWrapper.locationFlow.map { loc: Location -> GeoPoint(loc) }

    init {
        gpsProviderWrapper.addLocationConsumer { location, _ ->
            run {
                if (location != null
                    && location.latitude > 42.1900 && location.latitude < 51.404
                    && location.longitude > -4.932 && location.longitude < 8.341
                ) {
                    // todo if (location != loc)
                    viewModelScope.launch(Dispatchers.IO) {
                        val pois = repo.getPois(GeoPoint(location))
                        Log.e("MapViewModel", "received ${pois.size}: POIs")
                        mutablePOIsList.emit(pois)
                    }
                }
            }
        }
    }
}