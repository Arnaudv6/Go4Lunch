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
import okhttp3.tls.HandshakeCertificates
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
    fun provideNominatimRetrofit(httpLoggingInterceptor: HttpLoggingInterceptor): PoiRetrofit {
        val client = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_NOMINATIM)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().serializeNulls().create())
            )
            .build()
            .create(PoiRetrofit::class.java)
    }

    @Singleton
    @Provides
    fun provideUsersRetrofit(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        @ApplicationContext context: Context
    ): UserRetrofit {
        val clientCertificates = HandshakeCertificates.Builder()
            .addPlatformTrustedCertificates()
            .addInsecureHost(BASE_DOMAIN_USERS)
            .build()

        // pin is easier than PEM file in context.resources.openRawResource(R.raw.arnaud)
        val certificatePinner = CertificatePinner.Builder()
            .add(BASE_DOMAIN_USERS, "sha256/siPA0fWSc2epRP1Q3E3Mgxxj0Re0vBzhBUgpt95lGng=")
            .build()

        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                clientCertificates.sslSocketFactory(),
                clientCertificates.trustManager
            )
            .certificatePinner(certificatePinner)
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

