package com.cleanup.go4lunch.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MapViewModel @Inject constructor(
    private val poiRepository: PoiRepository,
    private val settingsRepository: SettingsRepository,
    // private val locationUtils: LocationUtils,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()
    val initialMapBox = settingsRepository.boxFlow
    val poiListFlow = poiRepository.poisFromCache

    fun requestPoiPins(boundingBox: BoundingBox) {
        viewModelScope.plus(Dispatchers.IO).launch {
            val poiList = poiRepository.getPOIsInBox(boundingBox)
            if (poiList != null)
                poiRepository.putPOIsInCache(poiList)
        }
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionChannel.trySend(MapViewAction.CenterOnMe(GeoPoint(location)))
    }

    fun closingMap(boundingBox: BoundingBox) {
        settingsRepository.setMapBox(
            BoxEntity(
                boundingBox.actualNorth,
                boundingBox.actualSouth,
                boundingBox.lonWest,
                boundingBox.lonEast
            )
        )
    }


}