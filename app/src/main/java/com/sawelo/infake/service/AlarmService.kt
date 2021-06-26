package com.sawelo.infake.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.R
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "infake_id"
        const val CHANNEL_NAME = "Infake Channel"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var alarmManager: AlarmManager
    private lateinit var flutterStartUpServicePendingIntent: PendingIntent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /**
        Notes:
        use setAndAllowWhileIdle() or setExactAndAllowWhileIdle() to guarantee that the alarms will execute
        use setInexactRepeating() to reduces number of system wake

        use alarm "wakeup" version to ensure flutter warm-up can be completed in 10 seconds time frame

        for interval type, use elapsed real time (ELAPSED_REALTIME_WAKEUP)
        for specific time, use clock-based real time (RTC_WAKEUP)

        TODO: use alarmManager.cancel(pendingIntent) to cancel a PendingIntent
         * */

        Log.d("AlarmService", "Starting AlarmService")

        val sharedPref = SharedPrefFunction(this)
        val activeTime = "${sharedPref.activeHour}:${sharedPref.activeMinute}"

        /** Create notificationManager to ensure functionality */
        val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN)

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        // Build Notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Infake")
                .setContentText("Preparing call for $activeTime")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        // Create Intent & PendingIntent to start FlutterStartUpService
        val flutterStartUpServiceIntent = Intent(this, FlutterStartUpService::class.java)
        flutterStartUpServicePendingIntent = PendingIntent.getService(
            this, 0, flutterStartUpServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // Create Intent to NotificationService
        val notificationServiceIntent = Intent(this, NotificationService::class.java)

        // Create AlarmManager
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Setting RCT_Wakeup directly to FlutterStartUpService
        fun setSpecificAlarm() {
            Log.d("AlarmService", "Run setSpecificAlarm() for $activeTime")
            var activeMinute: Int = sharedPref.activeMinute
            val preparingMinute: Int = if (--activeMinute > 0) --activeMinute else activeMinute
            val c: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, sharedPref.activeHour)
                set(Calendar.MINUTE, preparingMinute)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        c.timeInMillis,
                        flutterStartUpServicePendingIntent)
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        c.timeInMillis,
                        flutterStartUpServicePendingIntent)
            }

        }

        stopService(flutterStartUpServiceIntent)
        stopService(notificationServiceIntent)
        setSpecificAlarm()
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    override fun onDestroy() {
        alarmManager.cancel(flutterStartUpServicePendingIntent)
        println("AlarmService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}