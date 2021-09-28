package com.cleanup.go4lunch.ui.map

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text


    fun getLocation(): Flow<GeoPoint> {
        return repo.getLocationFlow().map { loc: Location -> GeoPoint(loc) }
    }

    fun getPointsOfInterest(): Flow<ArrayList<POI>> {
        return repo.getPointsOfInterest()
    }

}