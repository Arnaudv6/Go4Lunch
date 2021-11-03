package com.cleanup.go4lunch.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MapViewModel @Inject constructor(
    private val useCase: UseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val gpsProviderWrapper: GpsProviderWrapper
) : ViewModel() {

    private val boundingBoxMutableStateFlow = MutableStateFlow(BoundingBox())
    val viewActionLiveEvent = SingleLiveEvent<MapViewAction>()

    init {
        viewModelScope.launch {
            viewActionLiveEvent.value = MapViewAction.InitialBox(useCase.getInitialBox())
        }
    }

    val viewStateLiveData: LiveData<MapViewState> =
        combine(useCase.cachedPOIsListFlow, useCase.matesListFlow) { poiList, usersList ->
            MapViewState(
                poiList.map {
                    val going = usersList.filter { user ->
                        user.goingAtNoon == it.id
                    }.map { user -> user.firstName }
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
            val numberOfPoi =
                MapViewAction.PoiRetrieval(useCase.fetchPOIsInBoundingBox(boundingBox))
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
            useCase.setMapBox(
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

