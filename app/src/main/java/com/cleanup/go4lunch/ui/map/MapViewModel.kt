package com.cleanup.go4lunch.ui.map

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.FranceGps
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
                if (location != null) {
                    val geoPoint = GeoPoint(location)
                    if (FranceGps.isDeviceLocation(geoPoint) && FranceGps.inFrance(geoPoint)) (
                            viewModelScope.launch(Dispatchers.IO) {
                                val pois = repo.getPois(geoPoint)
                                Log.e("MapViewModel", "received ${pois.size}: POIs")
                                mutablePOIsList.emit(pois)
                            })
                }
            }
        }
    }
}