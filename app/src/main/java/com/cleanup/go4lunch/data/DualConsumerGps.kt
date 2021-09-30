package com.cleanup.go4lunch.data

import android.content.Context
import android.location.Location
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

class DualConsumerGps(context: Context?, private val repositoryConsumer: RepositoryConsumer) :
    GpsMyLocationProvider(context), IMyLocationConsumer {

    private var locationConsumer: IMyLocationConsumer? = null

    override fun startLocationProvider(
        myLocationConsumer: IMyLocationConsumer?
    ): Boolean {
        locationConsumer = myLocationConsumer
        return super.startLocationProvider(this)
    }

    interface RepositoryConsumer {
        fun executeAlso(location: Location?)
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        repositoryConsumer.executeAlso(location)
        locationConsumer?.onLocationChanged(location, this)
    }
}

