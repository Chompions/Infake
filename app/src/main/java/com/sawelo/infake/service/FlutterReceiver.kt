package com.sawelo.infake.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.R
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.function.IntentFunction
import java.util.concurrent.TimeUnit

class FlutterReceiver : BroadcastReceiver() {

    companion object {
        lateinit var stopTimer: CountDownTimer
        var timerStarted: Boolean = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("FlutterReceiver", "Starting FlutterReceiver")
        if (context != null) {
            Log.d("FlutterReceiver", "Context is not null")
            val intentFunction = IntentFunction(context)
            FlutterFunction().createFlutterEngine(context)

            // Build Notification
            val builder = NotificationCompat.Builder(context, AlarmService.CHANNEL_ID)
                .setContentTitle("Infake")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .addAction(R.drawable.ic_baseline_cancel, "Cancel",
                    intentFunction.callDeclineService(System.currentTimeMillis().toInt()))

            // Countdown until FlutterReceiver stops
            stopTimer = object: CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()

                    if (secondsUntilFinished > 1) {
                        builder.setContentText("Incoming call in $secondsUntilFinished seconds")
                    } else {
                        builder.setContentText("Incoming call in 1 second")
                    }

                    intentFunction.notificationManager.notify(
                        AlarmService.NOTIFICATION_ID, builder.build())
                    Log.d("FlutterReceiver", "Countdown: $secondsUntilFinished")
                }
                override fun onFinish() {
                    intentFunction.cancelCall(
                        destroyFlutterEngine = false,
                        destroyAlarmService = true)
                    context.startService(intentFunction.notificationServiceIntent)
                }
            }
            timerStarted = true

            stopTimer.cancel()
            stopTimer.start()
        } else {
            Log.d("FlutterReceiver", "Context is null")
        }
    }


}