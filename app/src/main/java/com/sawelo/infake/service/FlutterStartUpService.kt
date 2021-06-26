package com.sawelo.infake.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.ContactData
import com.sawelo.infake.R
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.function.SharedPrefFunction
import java.util.concurrent.TimeUnit

class FlutterStartUpService : Service() {

    private lateinit var stopTimer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("FlutterStartUpService", "Starting FlutterStartUpService")

        val sharedPref = SharedPrefFunction(this)

        FlutterFunction().createFlutterEngine(this)
        Log.d("FlutterStartUpService", "Active data: " +
                "${sharedPref.activeName}, ${sharedPref.activeNumber}, ${sharedPref.activeRoute}")

        // Create Intent to AlarmService
        val alarmServiceIntent = Intent(this, AlarmService::class.java)
        // Create Intent to NotificationService
        val notificationServiceIntent = Intent(this, NotificationService::class.java)

        // Build Notification
        val builder = NotificationCompat.Builder(this, AlarmService.CHANNEL_ID)
            .setContentTitle("Infake")
            .setContentText("Starting up...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)

        // Countdown until FlutterStartUpService stops
        stopTimer = object: CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                Log.d("FlutterStartUpService", "Countdown: $secondsUntilFinished")
            }
            override fun onFinish() {
                stopService(alarmServiceIntent)
                stopSelf()
                FlutterFunction().sendMethodCall(
                    ContactData(
                        sharedPref.activeName,
                        sharedPref.activeNumber,
                        sharedPref.activeRoute,
                    )
                )
                startService(notificationServiceIntent)
            }
        }

        stopTimer.start()
        startForeground(AlarmService.NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer.cancel()
        println("FlutterStartUpService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}