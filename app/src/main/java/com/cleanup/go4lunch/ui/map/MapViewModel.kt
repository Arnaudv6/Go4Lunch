package com.cleanup.go4lunch.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.LocationUtils
import com.cleanup.go4lunch.data.repository.PoiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: PoiRepository,
    private val locationUtils: LocationUtils,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val boundingBoxMutableStateFlow = MutableStateFlow<BoundingBox?>(null)

    private var mapBox: MutableStateFlow<BoundingBox> =
        MutableStateFlow<BoundingBox>(BoundingBox(1.0, 1.0, 1.0, 1.0))

    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    val poisList = MutableStateFlow<List<POI>>(emptyList())
    /*
    gpsProviderWrapper.locationFlow.combine(boundingBoxMutableStateFlow) { location, boundingBox ->
        val geoPoint = GeoPoint(location)

        if (boundingBox != null) {
            repo.getPOIsInBox(boundingBox)
        } else if (locationUtils.isDeviceLocation(geoPoint) && locationUtils.inFrance(geoPoint)) {
            repo.getPOIsNearGeoPoint(geoPoint)
        } else {
            emptyList()
        }
    }.stateIn()
    */

    init {
        viewActionChannel.trySend(MapViewAction.InitialMapBox(BoundingBox()))

    }

    // todo save and restore view box
    // todo: debouncing belongs in VM
    fun mapBoxChanged(boundingBox: BoundingBox) {
        Log.e("MapViewModel", "here we request POIs")
        boundingBoxMutableStateFlow.tryEmit(boundingBox)
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionChannel.trySend(MapViewAction.CenterOnMe(GeoPoint(location)))
    }
}