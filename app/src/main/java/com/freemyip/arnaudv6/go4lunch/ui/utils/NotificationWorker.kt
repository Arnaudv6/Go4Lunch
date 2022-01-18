package com.freemyip.arnaudv6.go4lunch.ui.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiMapperDelegate
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.useCase.SessionUserUseCase
import com.freemyip.arnaudv6.go4lunch.ui.detail.DetailsActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate

// https://developer.android.com/reference/androidx/hilt/work/HiltWorker
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted @NonNull parameters: WorkerParameters,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val sessionUserUseCase: SessionUserUseCase,
    private val poiRepository: PoiRepository,
) : CoroutineWorker(context, parameters) {
    companion object {
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL"
        private const val REQUEST_CODE = 4445
    }

    // TODO NINO
    //  java.lang.RuntimeException: Unable to create service androidx.work.impl.background.systemjob.SystemJobService: java.lang.IllegalStateException: WorkManager needs to be initialized via a ContentProvider#onCreate() or an Application#onCreate().
    //  pourquoi on ne laisse pas android initialiser lui-même le workManager?
    //  pourquoi il ne sait pas utiliser mon Provider (dans DataModule) ici?
    //  annoter @AndroidEntryPoint, ça règlerait le PB?

    override suspend fun doWork(): Result {
        val session = sessionUserUseCase.sessionUserFlow.filterNotNull().first()
        val list = poiRepository.cachedPOIsListFlow.filterNotNull().first()

        session.user.goingAtNoon?.let { list.firstOrNull { poi -> poi.id == it } }?.let {
            val text = poiMapperDelegate.nameCuisineAndAddress(it.name, it.cuisine, it.address)


            val pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                DetailsActivity.navigate(context, it.id),
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
                .setContentText(text)
                .addAction(
                    R.drawable.ic_baseline_restaurant_menu_24,
                    context.getString(R.string.your_lunch),
                    pendingIntent
                )
                .build()

            val notificationUID = LocalDate.now().toEpochDay().toInt()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancelAll()
//            notificationManager.createNotificationChannel(channel) TODO ARNAUD
            notificationManager.notify(notificationUID, notification)

        }

        return Result.success()
    }
}




