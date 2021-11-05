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
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    companion object {
        private const val BASE_URL_NOMINATIM: String = "https://nominatim.openstreetmap.org/"
        private const val BASE_DOMAIN_USERS: String = "192.168.1.79"
        private const val BASE_URL_USERS: String = "https://192.168.1.79:22280/"
    }

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

    @Singleton
    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao = appDatabase.settingsDao

    @Singleton
    @Provides
    fun providePoiDao(appDatabase: AppDatabase): PoiDao = appDatabase.poiDao


    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory
        .create(GsonBuilder().setLenient().serializeNulls().create())

    @Singleton
    @Provides
    fun provideNominatimRetrofit(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        gsonConverterFactory: GsonConverterFactory,
    ): PoiRetrofit {
        val client = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_NOMINATIM)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(PoiRetrofit::class.java)
    }

    @Singleton
    @Provides
    fun provideUsersRetrofit(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        gsonConverterFactory: GsonConverterFactory,
        @ApplicationContext context: Context
    ): UserRetrofit {
        val certificatePinner = CertificatePinner.Builder()
            .add(BASE_DOMAIN_USERS, "sha256/dW/tJSIXIW90ICQWo6Ib02vc5/YqcHeg8wxbyWU6rtI=")
            .build()

        val client = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(httpLoggingInterceptor)
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL_USERS)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(UserRetrofit::class.java)
    }

}

