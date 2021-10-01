package com.cleanup.go4lunch.data

import android.location.Location
import com.cleanup.go4lunch.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsProviderWrapper @Inject constructor() : IMyLocationConsumer, IMyLocationProvider {
    // IMyLocationConsumer interface implemented to pass "this" to GpsMyLocationProvider here
    // IMyLocationProvider interface implemented to pass "this" to MyLocationNewOverlay in MapFragment

    private val provider = GpsMyLocationProvider(MainApplication.instance)
    private val listeners = mutableSetOf<IMyLocationConsumer>()

    private val mutableLocationFlow = MutableStateFlow(
        Location("repository").apply {
            latitude = FranceGps.fallbackLatitude
            longitude = FranceGps.fallbackLongitude
            altitude = FranceGps.fallbackAltitude
        }
    )
    val locationFlow = mutableLocationFlow.asStateFlow()

    init {
        provider.startLocationProvider(this)
        provider.locationUpdateMinDistance = 10F  // float, meters
        provider.locationUpdateMinTime = 5000  // long, milliseconds
    }

    fun addLocationConsumer(consumer: IMyLocationConsumer?) {
        consumer?.let {
            listeners.add(it)
        }
    }

    fun removeLocationConsumer(listener: IMyLocationConsumer?) {
        listeners.remove(listener)
    }

    // CONSUMER IMyLocationConsumer
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        location?.let {
            mutableLocationFlow.value = it
        }

        listeners.forEach {
            it.onLocationChanged(location, this)
        }
    }

    // IMyLocationProvider
    override fun startLocationProvider(myLocationConsumer: IMyLocationConsumer?): Boolean {
        addLocationConsumer(myLocationConsumer)
        return provider.startLocationProvider(this)
    }

    // IMyLocationProvider
    override fun stopLocationProvider() {
        return  // I should call stopLocationProvider() only when activity goes Stopped I think
        provider.stopLocationProvider()
    }

    // IMyLocationProvider
    override fun getLastKnownLocation(): Location? {
        return provider.lastKnownLocation
    }

    // IMyLocationProvider
    override fun destroy() {
        return  // view calls destroy when fragment dies: we MUST survive.
        provider.destroy()
    }
}

