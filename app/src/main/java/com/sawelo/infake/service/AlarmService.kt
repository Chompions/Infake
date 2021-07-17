package com.sawelo.infake.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.R
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "infake_id"
        const val CHANNEL_NAME = "Infake Channel"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var alarmManager: AlarmManager
    private lateinit var flutterStartUpServicePendingIntent: PendingIntent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("AlarmService", "Starting AlarmService")

        val sharedPref = SharedPrefFunction(this)
        val intentFunction = IntentFunction(this)

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN)

            // Register the channel with the system
            intentFunction.notificationManager.createNotificationChannel(channel)
        }

        // Build Notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Infake")
                .setContentText("Preparing call for ${sharedPref.scheduleText}")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_baseline_cancel, "Cancel",
                    intentFunction.callDeclineService(System.currentTimeMillis().toInt()))

        // Create Intent & PendingIntent to start FlutterStartUpService
        flutterStartUpServicePendingIntent = PendingIntent.getService(
            this, 0, intentFunction.flutterStartUpServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Create AlarmManager
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Setting RCT_Wakeup directly to FlutterStartUpService
        fun setSpecificAlarm() {
            Log.d("AlarmService", "Run setSpecificAlarm() for ${sharedPref.scheduleText}")
            val c: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, sharedPref.specificHour)
                set(Calendar.MINUTE, sharedPref.specificMinute)
            }

            /**
             * while setAndAllowWhileIdle() or setExactAndAllowWhileIdle() was meant
             * to guarantee alarms execution, should be noted it's not exact while in
             * idle mode, it runs only in every 15 minutes
             *
             * To avoid Doze mode, use setAlarmClock()
             * */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(
                        c.timeInMillis,
                        flutterStartUpServicePendingIntent),
                    flutterStartUpServicePendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    c.timeInMillis,
                    flutterStartUpServicePendingIntent)
            }
        }

        // Setting ELAPSED_REALTIME_WAKEUP directly to FlutterStartUpService
        fun setRelativeAlarm() {
            Log.d("AlarmService", "Run setRelativeAlarm() for ${sharedPref.scheduleText}")

            val assignedHour: Long = TimeUnit.HOURS.toMillis(sharedPref.relativeHour.toLong())
            val assignedMinute: Long = TimeUnit.MINUTES.toMillis(sharedPref.relativeMinute.toLong())
            val assignedSecond: Long = TimeUnit.SECONDS.toMillis(sharedPref.relativeSecond.toLong())
            val assignedEntireMillis: Long = assignedHour + assignedMinute + assignedSecond

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + assignedEntireMillis,
                    flutterStartUpServicePendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + assignedEntireMillis,
                    flutterStartUpServicePendingIntent
                )
            }
        }

        // Cancel everything before starting alarm
        intentFunction.cancelCall()

        // Determine which alarm to set according to user choice
        if (sharedPref.timerType) {
            setRelativeAlarm()
        } else {
            setSpecificAlarm()
        }
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    override fun onDestroy() {
        alarmManager.cancel(flutterStartUpServicePendingIntent)
        Log.d("Destroy", "AlarmService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}