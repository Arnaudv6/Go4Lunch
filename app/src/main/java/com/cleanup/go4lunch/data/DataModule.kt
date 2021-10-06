package com.cleanup.go4lunch.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.data.pois.PoiDao
import com.cleanup.go4lunch.data.settings.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "MyDataBase")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao{
        return appDatabase.settingsDao
    }

    @Provides
    fun providePoiDao(appDatabase: AppDatabase): PoiDao{
        return appDatabase.poiDao
    }

}
