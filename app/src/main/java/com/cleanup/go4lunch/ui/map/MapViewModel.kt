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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import org.osmdroid.bonuspack.location.POI
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

    init {
        val mapBox = settingsRepository.getMapBox()
        if (mapBox != null)
            viewActionChannel.trySend(MapViewAction.InitialMapBox(mapBox))
    }

    val pointOfInterestListStateFlow: StateFlow<List<POI>> =
        boundingBoxMutableStateFlow.debounce(3000).map {
            if (it == null) {
                emptyList()
            } else {
                settingsRepository.setMapBox(
                    BoxEntity(
                        it.actualNorth,
                        it.actualSouth,
                        it.lonWest,
                        it.lonEast
                    )
                )
                poiRepository.getPOIsInBox(it)
            }
        }.stateIn(viewModelScope.plus(Dispatchers.IO), SharingStarted.Lazily, emptyList())

    fun mapBoxChanged(boundingBox: BoundingBox) {
        boundingBoxMutableStateFlow.tryEmit(boundingBox)
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionChannel.trySend(MapViewAction.CenterOnMe(GeoPoint(location)))
    }
}