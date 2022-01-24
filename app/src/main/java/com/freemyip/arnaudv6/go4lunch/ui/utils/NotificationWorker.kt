package com.freemyip.arnaudv6.go4lunch.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiEntity
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiMapperDelegate
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import com.freemyip.arnaudv6.go4lunch.domain.useCase.GetSynchronizedUserUseCase
import com.freemyip.arnaudv6.go4lunch.ui.detail.DetailsActivity
import com.freemyip.arnaudv6.go4lunch.ui.main.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate

// https://developer.android.com/reference/androidx/hilt/work/HiltWorker
@HiltWorker
@ExperimentalCoroutinesApi
class NotificationWorker
@AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted @NonNull parameters: WorkerParameters,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val synchronizedUserUseCase: GetSynchronizedUserUseCase,
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
) : CoroutineWorker(context, parameters) {
    companion object {
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL_ID"
        private const val REQUEST_CODE = 4445
    }

    override suspend fun doWork(): Result {
        val poiEntity: PoiEntity? = withTimeoutOrNull(4_000) {
            usersRepository.updateMatesList()
            val session = synchronizedUserUseCase().filterNotNull().first()
            val list = poiRepository.cachedPOIsListFlow.filterNotNull().first()
            session.user.goingAtNoon?.let {
                list.firstOrNull { poi -> poi.id == it }
            }
        }

        val text = poiEntity?.let {
            poiMapperDelegate.nameCuisineAndAddress(it.name, it.cuisine, it.address)
        } ?: "No internet, can't show lunch place"

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            poiEntity?.let { DetailsActivity.navigate(context, it.id) }
                ?: Intent(context, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .build()

        val notification = NotificationCompat
            .Builder(context, channel.id)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(context.getString(R.string.app_name))
            .setAutoCancel(true)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()

        val notificationUID = LocalDate.now().toEpochDay().toInt()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
        createNotificationChannel(notificationManager)
        notificationManager.notify(notificationUID, notification)
        return Result.success()
    }

    private fun createNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        // crappy google code. NotificationChannelCompat() works not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CHANNEL_NAME",  // put this in string, should other notifications be needed
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "descriptionText" }
            notificationManagerCompat.createNotificationChannel(channel)
        }
    }

}




