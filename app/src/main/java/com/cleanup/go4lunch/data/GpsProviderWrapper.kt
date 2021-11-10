package com.cleanup.go4lunch.data

import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsProviderWrapper @Inject constructor(private val provider: GpsMyLocationProvider) :
    IMyLocationConsumer, // to pass "this" to GpsMyLocationProvider here
    IMyLocationProvider  // to pass "this" to MyLocationNewOverlay in MapFragment
{

    companion object {
        private val EIFFEL_TOWER = GeoPoint(48.8583, 2.2944, 5.0)
    }

    private var myLocationNewOverlayListener: IMyLocationConsumer? = null

    private val mutableLocationFlow = MutableStateFlow(
        Location("repository").apply {  // todo this "apply" concept I could use in other places.
            latitude = EIFFEL_TOWER.latitude
            longitude = EIFFEL_TOWER.longitude
            altitude = EIFFEL_TOWER.altitude
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
    fun locationPermissionUpdate(fine: Boolean, coarse: Boolean) =
        possibleLocationMutableStateFlow.tryEmit(fine || coarse)

    // CONSUMER IMyLocationConsumer
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        location?.let {
            Log.d(GpsProviderWrapper::class.qualifiedName, "onLocationChanged: new position")
            mutableLocationFlow.value = it
        }
        @Suppress("DEPRECATION")
        myLocationNewOverlayListener?.onLocationChanged(location, this)
    }

    // IMyLocationProvider
    fun startLocationProvider(): Boolean = provider.startLocationProvider(this)
    // todo  java.lang.NullPointerException: Attempt to invoke virtual method
    //  'java.util.List android.location.LocationManager.getProviders(boolean)' on a null object reference

    // IMyLocationProvider
    override fun startLocationProvider(myLocationConsumer: IMyLocationConsumer?): Boolean {
        @Suppress("DEPRECATION")
        myLocationNewOverlayListener = myLocationConsumer
        return startLocationProvider()
    }

    fun stopWrapper() = provider.stopLocationProvider()

    // IMyLocationProvider
    override fun stopLocationProvider() = Unit

    // IMyLocationProvider
    override fun getLastKnownLocation(): Location? = provider.lastKnownLocation

    // IMyLocationProvider
    override fun destroy() = Unit

    fun destroyWrapper() = provider.destroy()

}

