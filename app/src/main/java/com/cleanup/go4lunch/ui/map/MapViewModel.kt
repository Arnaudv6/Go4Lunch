package com.cleanup.go4lunch.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.useCase.MatesByPlaceUseCase
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    matesByPlaceUseCase: MatesByPlaceUseCase,
    private val poiRepository: PoiRepository,
    private val settingsRepository: SettingsRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val boundingBoxMutableStateFlow = MutableStateFlow(BoundingBox())
    val viewActionLiveEvent = SingleLiveEvent<MapViewAction>()

    init {
        viewModelScope.launch {
            viewActionLiveEvent.value = MapViewAction.InitialBox(settingsRepository.getInitialBox())
        }
    }

    val viewStateLiveData: LiveData<MapViewState> =
        combine(
            poiRepository.cachedPOIsListFlow,
            matesByPlaceUseCase.matesByPlaceFlow
        ) { poiList, matesByPlace ->
            MapViewState(
                poiList.map {
                    MapViewState.Pin(
                        id = it.id,
                        name = it.name,
                        colleagues = matesByPlace[it.id]?.joinToString(
                            separator = ", ",
                            prefix = "going: "
                        ) ?: "",
                        icon = if (matesByPlace[it.id].isNullOrEmpty()) {
                            R.drawable.poi_orange
                        } else {
                            R.drawable.poi_green
                        },
                        location = GeoPoint(it.latitude, it.longitude)
                    )
                })
        }.asLiveData()

    fun requestPoiPins(boundingBox: BoundingBox) {
        viewModelScope.launch(ioDispatcher) {
            val numberOfPoi =
                MapViewAction.PoiRetrieval(poiRepository.fetchPOIsInBoundingBox(boundingBox))
            withContext(Dispatchers.Main) {
                viewActionLiveEvent.value = numberOfPoi
            }
        }
    }

    fun onCenterOnMeClicked() {
        val location = gpsProviderWrapper.locationFlow.value
        viewActionLiveEvent.value = MapViewAction.CenterOnMe(GeoPoint(location))
    }

    fun mapBoxChanged(box: BoundingBox) {
        // We always get a box, consisting of 4 doubles, that may be uninitialized => 0
        if (box != BoundingBox())
            boundingBoxMutableStateFlow.tryEmit(box)
    }

    fun onStop() {
        val boundingBox = boundingBoxMutableStateFlow.value
        // viewActionLiveEvent.value = MapViewAction.InitialBox(boundingBox)
        if (boundingBox != BoundingBox()) {
            settingsRepository.setMapBox(
                BoxEntity(
                    north = boundingBox.latNorth,
                    south = boundingBox.latSouth,
                    west = boundingBox.lonWest,
                    east = boundingBox.lonEast
                )
            )
        }
    }
}

