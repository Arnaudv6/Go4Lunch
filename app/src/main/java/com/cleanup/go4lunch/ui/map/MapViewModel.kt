package com.cleanup.go4lunch.ui.map

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val gpsProviderWrapper: GpsProviderWrapper,
    private val repo: Repository,
) : ViewModel() {

    fun getLocation(): Flow<GeoPoint> {
        return gpsProviderWrapper.locationFlow.map { loc: Location -> GeoPoint(loc) }
    }

    fun getPointsOfInterest(): Flow<ArrayList<POI>> {
        return repo.getPointsOfInterest()
    }

    fun getGps(): GpsMyLocationProvider {
        return repo.gps
    }
}