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

class FlutterStartUpService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("FlutterStartUpService", "Starting FlutterStartUpService")

        val sharedPref = SharedPrefFunction(this)

        FlutterFunction().createFlutterEngine(this)
        Log.d("FlutterStartUpService", "Active data: " +
                "${sharedPref.activeName}, ${sharedPref.activeNumber}, ${sharedPref.activeRoute}")

        // Build Notification
        val builder = NotificationCompat.Builder(this, AlarmService.CHANNEL_ID)
            .setContentTitle("Infake")
            .setContentText("Starting up Flutter")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)

        // Create Intent to start NotificationService
        val notificationServiceIntent = Intent(this, NotificationService::class.java)

        // Create Intent to stop AlarmService
        val alarmServiceIntent = Intent(this, AlarmService::class.java)

        // Countdown until FlutterStartUpService stops
        val stopTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("FlutterStartUpService", "Countdown: $millisUntilFinished")
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
        println("FlutterStartUpService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}