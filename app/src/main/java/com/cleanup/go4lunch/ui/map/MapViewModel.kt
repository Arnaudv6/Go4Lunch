package com.cleanup.go4lunch.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MapViewModel @Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val settingsRepository: SettingsRepository,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val boundingBoxMutableStateFlow = MutableStateFlow(BoundingBox())
    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow: Flow<MapViewAction> = viewActionChannel.receiveAsFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            viewActionChannel.trySend(MapViewAction.InitialBox(settingsRepository.boxFlow.first()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            poiRepository.poiDataRetrievalStateFlow.collect {
                if (it != Pair(0, 0)) viewActionChannel.trySend(MapViewAction.PoiRetrieval(it))
            }
        }
    }

    // TODO Transform to stateflow
    val viewStateFlow: Flow<MapViewState> = poiRepository.poisFromCache.map { poiList ->
        MapViewState(poiList.map {
            val going = usersRepository.usersGoing(it.id)
            MapViewState.Pin(
                id = it.id,
                name = it.name,
                colleagues = if (going.isNotEmpty()) going.joinToString(
                    separator = ", ",
                    prefix = "going: "
                ) else "",
                icon = if (going.isEmpty()) {
                    R.drawable.poi_orange
                } else {
                    R.drawable.poi_green
                },
                location = GeoPoint(it.latitude, it.longitude)
            )
        })
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

    fun mapBoxChanged(box: BoundingBox) {
        // We always get a box, consisting of 4 doubles, that may be uninitialized => 0
        if (box != BoundingBox())
            boundingBoxMutableStateFlow.tryEmit(box)
    }

    fun onStop() {
        val boundingBox = boundingBoxMutableStateFlow.value
        if (boundingBox != BoundingBox()) {
            settingsRepository.setMapBox(
                BoxEntity(
                    boundingBox.latNorth,
                    boundingBox.latSouth,
                    boundingBox.lonWest,
                    boundingBox.lonEast
                )
            )
        }
    }
}

