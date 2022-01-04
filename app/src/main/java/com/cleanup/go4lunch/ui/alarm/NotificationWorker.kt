package com.cleanup.go4lunch.ui.alarm

import android.app.Application
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
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiMapperDelegate
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

@HiltWorker
class NotificationWorker @Inject constructor(
    private val context: Context,
    @NonNull parameters: WorkerParameters,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val sessionUserUseCase: SessionUserUseCase,
    private val poiRepository: PoiRepository,
    private val application: Application
) : CoroutineWorker(context, parameters) {

    companion object {
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL"
        private const val REQUEST_CODE = 4445
    }

    override suspend fun doWork(): Result {
        Log.d(this.javaClass.canonicalName, "AlarmActivity init.")

        val session = sessionUserUseCase.sessionUserFlow.filterNotNull().first()
        val list = poiRepository.cachedPOIsListFlow.filterNotNull().first()

        session.user.goingAtNoon?.let { list.firstOrNull { poi -> poi.id == it } }?.let {

            val text = poiMapperDelegate.nameCuisineAndAddress(it.name, it.cuisine, it.address)

            val notificationUID = LocalDate.now().toEpochDay().toInt()

            val intent = DetailsActivity.navigate(context, it.id)

            val pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                intent,
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
                .setContentTitle(application.getString(R.string.app_name))
                .setContentText(text)
                .addAction(
                    R.drawable.ic_baseline_restaurant_menu_24,
                    application.getString(R.string.your_lunch),
                    pendingIntent
                )
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancelAll()
            notificationManager.notify(notificationUID, notification)

        }

        return Result.success()
    }
}




