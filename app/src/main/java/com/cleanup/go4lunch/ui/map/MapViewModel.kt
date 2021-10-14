package com.cleanup.go4lunch.ui.map

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.MainApplication
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val usersRepository: UsersRepository,
    private val settingsRepository: SettingsRepository,
    private val gpsProviderWrapper: GpsProviderWrapper,
    @ApplicationContext appContext: Context
) : ViewModel() {

    // todo Nino : comment je transforme ce Drawable? en Drawable, stp ?
    private val iconGreen =
        ResourcesCompat.getDrawable(appContext.resources, R.drawable.poi_green, null)
    private val iconOrange =
        ResourcesCompat.getDrawable(appContext.resources, R.drawable.poi_orange, null)

    private val boundingBoxMutableStateFlow = MutableStateFlow<BoundingBox?>(null)
    private val viewActionChannel = Channel<MapViewAction>(Channel.BUFFERED)
    val viewActionFlow = viewActionChannel.receiveAsFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            viewActionChannel.trySend(MapViewAction.InitialBox(settingsRepository.boxFlow.first()))
        }
    }

    // TODO Transform to stateflow
    val viewStateFlow = poiRepository.poisFromCache.map { poiList ->
        MapViewState(poiList.map {
            val going = usersRepository.usersGoing(it.id)
            MapViewState.Pin(
                it.id,
                it.name,
                going.joinToString(separator = ", ", postfix = "going: "),
                if (going.isEmpty()) iconOrange else iconGreen,
                GeoPoint(it.latitude, it.longitude)
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

