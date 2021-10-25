package com.cleanup.go4lunch.data

import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsProviderWrapper @Inject constructor(
    private val provider: GpsMyLocationProvider
) : IMyLocationConsumer, IMyLocationProvider {
    // IMyLocationConsumer interface implemented to pass "this" to GpsMyLocationProvider here
    // IMyLocationProvider interface implemented to pass "this" to MyLocationNewOverlay in MapFragment

    companion object {
        private const val DEPRECATION = "Only here for MyLocationNewOverlay's use."
    }

    @Deprecated(DEPRECATION)
    private var myLocationNewOverlayListener: IMyLocationConsumer? = null

    private val mutableLocationFlow = MutableStateFlow(
        Location("repository").apply {
            latitude = MyLocationUtils.fallbackLatitude
            longitude = MyLocationUtils.fallbackLongitude
            altitude = MyLocationUtils.fallbackAltitude
        }
    )
    val locationFlow: StateFlow<Location> = mutableLocationFlow.asStateFlow()

    init {
        provider.startLocationProvider(this)
        provider.locationUpdateMinDistance = 10F  // float, meters
        provider.locationUpdateMinTime = 5000  // long, milliseconds
    }

    private val possibleLocationMutableStateFlow = MutableStateFlow(false)
    val possibleLocation: Flow<Boolean> = possibleLocationMutableStateFlow
    fun locationPermissionUpdate(fine: Boolean, coarse: Boolean) {
        possibleLocationMutableStateFlow.tryEmit(fine || coarse)
    }

    // CONSUMER IMyLocationConsumer
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        location?.let {
            Log.e("wrapper", "onLocationChanged: nouvelle position")
            mutableLocationFlow.value = it
        }
        @Suppress("DEPRECATION")
        myLocationNewOverlayListener?.onLocationChanged(location, this)
    }

    // IMyLocationProvider
    fun startLocationProvider(): Boolean {
        return provider.startLocationProvider(this)
    }

    // IMyLocationProvider
    @Deprecated(DEPRECATION + "Use flow instead")
    override fun startLocationProvider(myLocationConsumer: IMyLocationConsumer?): Boolean {
        @Suppress("DEPRECATION")
        myLocationNewOverlayListener = myLocationConsumer
        return startLocationProvider()
    }

    fun stopWrapper() {
        provider.stopLocationProvider()
    }

    // IMyLocationProvider
    @Deprecated(DEPRECATION)
    override fun stopLocationProvider() {
        return
    }

    // IMyLocationProvider
    override fun getLastKnownLocation(): Location? {
        return provider.lastKnownLocation
    }

    // IMyLocationProvider
    @Deprecated(DEPRECATION)
    override fun destroy() {
        return
    }

    fun destroyWrapper() {
        provider.destroy()
    }
}

