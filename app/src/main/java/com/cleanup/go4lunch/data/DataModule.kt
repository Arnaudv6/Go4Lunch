package com.cleanup.go4lunch.data

import android.app.Application
import com.cleanup.go4lunch.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideGpsMyLocationProvider(
        application: Application
    ): GpsMyLocationProvider = GpsMyLocationProvider(application)

    @Provides
    @Singleton
    fun provideNominatimPOIProvider(): NominatimPOIProvider =
        NominatimPOIProvider(BuildConfig.APPLICATION_ID)


}