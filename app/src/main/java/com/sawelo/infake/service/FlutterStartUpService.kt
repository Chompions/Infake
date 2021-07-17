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

class FlutterStartUpService : Service() {

    private lateinit var stopTimer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("FlutterStartUpService", "Starting FlutterStartUpService")

        val intentFunction = IntentFunction(this)
        FlutterFunction().createFlutterEngine(this)

        // Build Notification
        val builder = NotificationCompat.Builder(this, AlarmService.CHANNEL_ID)
            .setContentTitle("Infake")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .addAction(R.drawable.ic_baseline_cancel, "Cancel",
                intentFunction.callDeclineService(System.currentTimeMillis().toInt()))

        // Countdown until FlutterStartUpService stops
        stopTimer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()

                if (secondsUntilFinished > 1) {
                    builder.setContentText("Incoming call in $secondsUntilFinished seconds")
                } else {
                    builder.setContentText("Incoming call in 1 second")
                }

                startForeground(AlarmService.NOTIFICATION_ID, builder.build())
                Log.d("FlutterStartUpService", "Countdown: $secondsUntilFinished")
            }
            override fun onFinish() {
                intentFunction.cancelCall(destroyFlutterEngine = false, destroyAlarmService = true)
                startService(intentFunction.notificationServiceIntent)
            }
        }

        stopTimer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer.cancel()
        Log.d("Destroy", "FlutterStartUpService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}