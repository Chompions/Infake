package com.sawelo.infake.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.R
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.function.IntentFunction
import java.util.concurrent.TimeUnit

class FlutterService : Service() {

    companion object {
        var stopTimer: CountDownTimer? = null
    }

    private lateinit var intentFunction: IntentFunction
    private lateinit var builder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FlutterService", "Starting FlutterService")
        FlutterFunction().createFlutterEngine(this)

        intentFunction = IntentFunction(this)

        // Build Notification
        builder = NotificationCompat.Builder(this, AlarmService.CHANNEL_ID)
            .setContentTitle("Infake")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .addAction(
                R.drawable.ic_baseline_cancel, "Cancel",
                intentFunction.callDeclineService(System.currentTimeMillis().toInt())
            )

        Log.d("FlutterService", "Starting stopTimer")
        // Countdown until FlutterReceiver stops
        stopTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished =
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()

                if (secondsUntilFinished > 1) {
                    builder.setContentText("Incoming call in $secondsUntilFinished seconds")
                } else {
                    builder.setContentText("Incoming call in 1 second")
                }

                intentFunction.notificationManager.notify(AlarmService.NOTIFICATION_ID, builder.build())
                Log.d("FlutterReceiver", "Countdown: $secondsUntilFinished")
            }

            override fun onFinish() {
                intentFunction.cancelCall(
                    destroyFlutterEngine = false,
                    destroyAlarmService = true
                )
                startService(intentFunction.notificationServiceIntent)
            }
        }.start()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("Destroy", "FlutterService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}