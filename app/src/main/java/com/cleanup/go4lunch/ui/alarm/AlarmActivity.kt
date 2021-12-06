package com.cleanup.go4lunch.ui.alarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiMapperDelegate
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    @Inject
    lateinit var poiMapperDelegate: PoiMapperDelegate

    /* 2021-12-03 17:49:17.746 10456-10456/com.cleanup.go4lunch E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.cleanup.go4lunch, PID: 10456
    java.lang.RuntimeException: Unable to instantiate activity ComponentInfo{com.cleanup.go4lunch/com.cleanup.go4lunch.ui.alarm.AlarmActivity}: java.lang.IllegalStateException: Your activity is not yet attached to the Application instance. You can't request ViewModel before onCreate call.
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3365)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3601)
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:85)
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2066)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:223)
        at android.app.ActivityThread.main(ActivityThread.java:7656)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:592)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:947)
     Caused by: java.lang.IllegalStateException: Your activity is not yet attached to the Application instance. You can't request ViewModel before onCreate call.
        at androidx.activity.ComponentActivity.getDefaultViewModelProviderFactory(ComponentActivity.java:529)
        at com.cleanup.go4lunch.ui.alarm.Hilt_AlarmActivity.getDefaultViewModelProviderFactory(Hilt_AlarmActivity.java:73)
        at com.cleanup.go4lunch.ui.alarm.AlarmActivity$special$$inlined$viewModels$default$1.invoke(ActivityViewModelLazy.kt:44)
        at com.cleanup.go4lunch.ui.alarm.AlarmActivity$special$$inlined$viewModels$default$1.invoke(Unknown Source:0)
        at androidx.lifecycle.ViewModelLazy.getValue(ViewModelProvider.kt:52)
        at androidx.lifecycle.ViewModelLazy.getValue(ViewModelProvider.kt:41)
        at com.cleanup.go4lunch.ui.alarm.AlarmActivity.getViewModel(AlarmActivity.kt:22)
        at com.cleanup.go4lunch.ui.alarm.AlarmActivity.<init>(AlarmActivity.kt:33)
        at java.lang.Class.newInstance(Native Method)
        at android.app.AppComponentFactory.instantiateActivity(AppComponentFactory.java:95)
        at androidx.core.app.CoreComponentFactory.instantiateActivity(CoreComponentFactory.java:45)
        at android.app.Instrumentation.newActivity(Instrumentation.java:1253)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3353)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3601) 
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:85) 
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135) 
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2066) 
        at android.os.Handler.dispatchMessage(Handler.java:106) 
        at android.os.Looper.loop(Looper.java:223) 
        at android.app.ActivityThread.main(ActivityThread.java:7656) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:592) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:947) 

     */


    companion object {
        private const val CHANNEL_ID = "GO4LUNCH_NOTIFICATION_CHANNEL"
        private const val REQUEST_CODE = 4445
    }

    init {
        Log.d(this.javaClass.canonicalName, "AlarmActivity init.")
        viewModel.lunchPlaceLiveData.value?.let {
            val text = poiMapperDelegate.nameCuisineAndAddress(it.name, it.cuisine, it.address)

            val notificationUID = LocalDate.now().toEpochDay().toInt()

            val intent = DetailsActivity.navigate(this, it.id)

            @SuppressLint("UnspecifiedImmutableFlag") // API 24+
            val pendingIntent = PendingIntent.getActivity(
                this, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT
            )

            val channel = NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .build()

            val notification = NotificationCompat
                .Builder(this, channel.id)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(application.getString(R.string.app_name))
                .setContentText(text)
                .addAction(
                    R.drawable.ic_baseline_restaurant_menu_24,
                    application.getString(R.string.your_lunch),
                    pendingIntent
                )
                .build()

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.cancelAll()
            notificationManager.notify(notificationUID, notification)
        }
    }
}




