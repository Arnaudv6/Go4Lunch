package com.cleanup.go4lunch

import android.app.Application
import com.cleanup.go4lunch.repository.Repository
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import javax.inject.Inject

@HiltAndroidApp()
//@InstallIn(MainActivity::class)
class Go4LunchApplication : Application() {

}

