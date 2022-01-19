package com.freemyip.arnaudv6.go4lunch.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiEntity
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiMapperDelegate
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.useCase.SessionUserUseCase
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import com.freemyip.arnaudv6.go4lunch.ui.detail.DetailsActivity
import com.freemyip.arnaudv6.go4lunch.ui.main.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// https://developer.android.com/reference/androidx/hilt/work/HiltWorker
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted @NonNull parameters: WorkerParameters,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val sessionUserUseCase: SessionUserUseCase,
    private val poiRepository: PoiRepository,
    private val workManager: WorkManager,
    private val clock: Clock,
    private val usersRepository: UsersRepository,
) : CoroutineWorker(context, parameters) {
    companion object {
        private const val WORKER_ID_NAME = "NOTIFICATION WORKER"
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL_ID"
        private const val REQUEST_CODE = 4445

        // todo Nino : static OK?
        fun setNotification(
            context: Context,
            workManager: WorkManager,
            clock: Clock,
            enable: Boolean
        ) {
            Log.d(this::class.java.canonicalName, "enableNotifications: $enable")
            if (enable) {
                workNextDayAtNoon(workManager, clock)
            } else {
                workManager.cancelUniqueWork(WORKER_ID_NAME)
                NotificationManagerCompat.from(context).cancelAll()
            }
        }

        fun workNextDayAtNoon(workManager: WorkManager, clock: Clock) {
            val nextLunch =
                if (LocalDateTime.now(clock).hour < 12) LocalDate.now(clock).atTime(LocalTime.NOON)
                else LocalDate.now(clock).plusDays(1).atTime(LocalTime.NOON)

            val seconds = LocalDateTime.now(clock).until(nextLunch, ChronoUnit.SECONDS)

            // builder.setInputData(). Also replace 'seconds' with '15', to test.
            val work = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(seconds, TimeUnit.SECONDS).build()

            workManager.beginUniqueWork(WORKER_ID_NAME, ExistingWorkPolicy.REPLACE, work).enqueue()
        }
    }

    override suspend fun doWork(): Result {
        // Todo Nino, timeout OK?
        var poiEntity: PoiEntity? = null
        coroutineScope {
            val job = this.launch {
                usersRepository.updateMatesList()
                val session = sessionUserUseCase.sessionUserFlow.filterNotNull().first()
                val list = poiRepository.cachedPOIsListFlow.filterNotNull().first()
                poiEntity = session.user.goingAtNoon?.let {
                    list.firstOrNull { poi -> poi.id == it }
                }
            }
            delay(4000)
            job.cancel()
            job.join()
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

        // todo Nino : is there a race-condition, here? Can I launch and un-attach?
        workNextDayAtNoon(workManager, clock)
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




