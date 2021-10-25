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
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

    val viewStateLiveData: LiveData<MapViewState> = poiRepository.poisFromCache.map { poiList ->
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
    }.asLiveData()

    fun requestPoiPins(boundingBox: BoundingBox) {
        viewModelScope.launch(ioDispatcher) {
            viewActionLiveEvent.value =
                MapViewAction.PoiRetrieval(poiRepository.getPOIsInBox(boundingBox))
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

