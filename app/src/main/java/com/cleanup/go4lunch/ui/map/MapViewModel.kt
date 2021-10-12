package com.cleanup.go4lunch.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

    private val boundingBoxMutableStateFlow = MutableStateFlow<BoundingBox?>(null)
    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    // TODO Transform to stateflow
    val viewStateFlow: Flow<MapViewState> = combine(
        settingsRepository.boxFlow,
        poiRepository.poisFromCache
    ) { initialMapBox, poiListFlow ->
        MapViewState(initialMapBox, poiListFlow)
    }

    fun requestPoiPins(boundingBox: BoundingBox) {
        viewModelScope.launch(Dispatchers.IO) {
            poiRepository.getPOIsInBox(boundingBox)
        }
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionChannel.trySend(MapViewAction.CenterOnMe(GeoPoint(location)))
    }

    fun mapBoxChanged(box: BoundingBox?) {
        boundingBoxMutableStateFlow.tryEmit(box)
    }

    fun onStop() {
        val boundingBox = boundingBoxMutableStateFlow.value
        if (boundingBox != null) {
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
}

