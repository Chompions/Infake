package com.sawelo.infake.function

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.NotificationService
import com.sawelo.infake.R
import java.util.*

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "infake_id"
        const val CHANNEL_NAME = "Infake Channel"
        const val NOTIFICATION_ID = 1
    }

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

        val sharedPref = SharedPrefFunction(this)
        val activeTime = "${sharedPref.activeHour}:${sharedPref.activeMinute}"
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

        // TODO: Set ContentIntent to press for immediate start of call notificationService

        // Create Intent & PendingIntent to start NotificationService
        val notificationServiceIntent = Intent(this, NotificationService::class.java)
        val notificationServicePendingIntent: PendingIntent = PendingIntent.getService(
                this, 0, notificationServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Create AlarmManager
        val alarmManager: AlarmManager =
                this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Setting RCT_Wakeup
        fun setSpecificAlarm() {
            Log.d("AlarmFunction", "Run setSpecificAlarm() for $activeTime")
            val c: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, sharedPref.activeHour)
                set(Calendar.MINUTE, sharedPref.activeMinute)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        c.timeInMillis,
                        notificationServicePendingIntent)
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        c.timeInMillis,
                        notificationServicePendingIntent)
            }

        }

        setSpecificAlarm()
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}