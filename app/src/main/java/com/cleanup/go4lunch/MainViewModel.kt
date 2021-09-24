package com.cleanup.go4lunch

import android.location.Location
import androidx.lifecycle.ViewModel
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    fun updateLocation(location: Location) {
        if (location.latitude < 51.404 && location.latitude > 42.190
            && location.longitude < 8.341 && location.longitude > -4.932
        )
            repo.setLocation(location)
    }

}