package com.cleanup.go4lunch.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.FranceGps
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.repository.PoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: PoiRepository,
    private val franceGps: FranceGps,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val boundingBoxMutableStateFlow = MutableStateFlow<BoundingBox?>(null)
    val poisList =
        gpsProviderWrapper.locationFlow.combine(boundingBoxMutableStateFlow) { location, boundingBox ->
            val geoPoint = GeoPoint(location)

            if (boundingBox != null) {
                repo.getPoisInBox(boundingBox)
            } else if (franceGps.isDeviceLocation(geoPoint) && franceGps.inFrance(geoPoint)) {
                repo.getPoisNearGeoPoint(geoPoint)
            } else {
                emptyList()
            }
        }.stateIn()

    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    fun mapBoxChanged(boundingBox: BoundingBox) {
        boundingBoxMutableStateFlow.tryEmit(boundingBox)
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionChannel.trySend(MapViewAction.Zoom(GeoPoint(location), 15.0, 1))
    }
}