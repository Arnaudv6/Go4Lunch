package com.cleanup.go4lunch.ui.alarm

import android.app.Application
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiMapperDelegate
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class AlarmActivity @Inject constructor(
    application: Application,
    poiMapperDelegate: PoiMapperDelegate
) : AppCompatActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    companion object {
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL"
    }

    init {
        viewModel.lunchPlaceLiveData.value?.let {
            val text = poiMapperDelegate.nameCuisineAndAddress(it.name, it.cuisine, it.address)

            val notificationUID = LocalDate.now().toEpochDay().toInt()

            val channel = NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .build()

            val notification = NotificationCompat
                .Builder(application, channel.id)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(application.getString(R.string.app_name))
                .setContentText(text)
                .build()

            val notificationManager = NotificationManagerCompat.from(application)
            notificationManager.cancelAll()
            notificationManager.notify(notificationUID, notification)

        }
    }

}




