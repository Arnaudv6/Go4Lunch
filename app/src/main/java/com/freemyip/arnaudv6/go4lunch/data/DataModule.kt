package com.freemyip.arnaudv6.go4lunch.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiDao
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRetrofit
import com.freemyip.arnaudv6.go4lunch.data.settings.SettingsDao
import com.freemyip.arnaudv6.go4lunch.data.users.UserRetrofit
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.text.similarity.FuzzyScore
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Keep  // annotated so we don't indefinitely wait for unreachable service in release builds.
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
    fun provideAllDispatchers() = AllDispatchers(Dispatchers.Main, Dispatchers.IO)

    @Provides
    @Singleton
    fun provideGpsMyLocationProvider(
        @ApplicationContext appContext: Context
    ): GpsMyLocationProvider = GpsMyLocationProvider(appContext)

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
    @Suppress("DEPRECATION") // deprecated in API 24+, we target 21
    fun provideFuzzyScore(application: Application): FuzzyScore =
        FuzzyScore(application.resources.configuration.locale)

    @Singleton
    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
        workerFactory: HiltWorkerFactory
    ): WorkManager {
        WorkManager.initialize(
            context,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
        return WorkManager.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun provideSurvivalInterceptor(): Interceptor = Interceptor {
        // an event listener on client does not work so we intercept.
        // finally: https://stackoverflow.com/questions/58697459
        try {
            it.proceed(it.request())
        } catch (e: Exception) { //IOException, SocketTimeoutException
            e.printStackTrace()
            Response.Builder()
                .request(it.request())
                .protocol(Protocol.HTTP_1_1)
                .code(999)
                .message("something bad happened")
                .body("{${e}}".toResponseBody())
                .build()
        }
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
        survivalInterceptor: Interceptor,
    ): PoiRetrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(survivalInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()

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
        survivalInterceptor: Interceptor,
        @ApplicationContext context: Context
    ): UserRetrofit {
        val certificatePinner = CertificatePinner.Builder()
            .add(BASE_DOMAIN_USERS, "sha256/dW/tJSIXIW90ICQWo6Ib02vc5/YqcHeg8wxbyWU6rtI=")
            .build()

        val client = OkHttpClient.Builder()
            .addInterceptor(survivalInterceptor)
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

    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}

