package com.cleanup.go4lunch.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.cleanup.go4lunch.data.pois.PoiDao
import com.cleanup.go4lunch.data.pois.PoiRetrofit
import com.cleanup.go4lunch.data.settings.SettingsDao
import com.cleanup.go4lunch.data.users.UserRetrofit
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideIoDispatcher() = Dispatchers.IO

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
        private const val BASE_URL_NOMINATIM: String = "https://nominatim.openstreetmap.org/"
        private const val BASE_URL_USERS: String = "http://192.168.1.79:22280/"
    }

    @Provides
    fun provideNominatimRetrofit(): PoiRetrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_NOMINATIM)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().serializeNulls().create())
            )
            .build()
            .create(PoiRetrofit::class.java)
    }

    @Provides
    fun provideUsersRetrofit(): UserRetrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            // todo remove that cleartext crap
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
            .addInterceptor(httpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL_USERS)
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().serializeNulls().create())
            )
            .build()
            .create(UserRetrofit::class.java)
    }

}

