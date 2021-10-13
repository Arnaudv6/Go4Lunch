package com.cleanup.go4lunch.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.cleanup.go4lunch.data.pois.PoiDao
import com.cleanup.go4lunch.data.pois.PoiRetrofit
import com.cleanup.go4lunch.data.settings.SettingsDao
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "MyDataBase")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao {
        return appDatabase.settingsDao
    }

    @Provides
    fun providePoiDao(appDatabase: AppDatabase): PoiDao {
        return appDatabase.poiDao
    }

    companion object {
        private const val BASE_URL: String = "https://nominatim.openstreetmap.org/"
    }

    @Provides
    fun provideNominatimRetrofit(): PoiRetrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(PoiRetrofit::class.java)
    }
}
