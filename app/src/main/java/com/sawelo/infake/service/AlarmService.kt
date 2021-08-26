package com.sawelo.infake.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sawelo.infake.R
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
import com.sawelo.infake.function.UpdateTextFunction
import java.util.*

class AlarmService: Service() {

    companion object {
        const val CHANNEL_ID = "infake_id"
        const val CHANNEL_NAME = "Infake Channel"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var alarmManager: AlarmManager
    private lateinit var flutterServicePendingIntent: PendingIntent
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var intentFunction: IntentFunction
    private lateinit var sharedPref: SharedPrefFunction

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("AlarmService", "Starting AlarmService")

        sharedPref = SharedPrefFunction(this)
        intentFunction = IntentFunction(this)

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            )

            // Register the channel with the system
            intentFunction.notificationManager.createNotificationChannel(channel)
        }

        // Determine which alarm to set according to user choice
        setAlarm()
        val (_, notificationText) =
            UpdateTextFunction(this)
                .updateMainText(sharedPref.scheduleData)

        // Build Notification
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Infake")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_baseline_cancel, "Cancel",
                intentFunction.callDeclineService(System.currentTimeMillis().toInt())
            )

        intentFunction.cancelMethod()
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    // Setting RCT_Wakeup directly to FlutterReceiver
    private fun setAlarm() {
        Log.d("AlarmService", "Run setAlarm()")

        // Create AlarmManager
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        @SuppressLint("UnspecifiedImmutableFlag")
        flutterServicePendingIntent = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.getService(
                this, 0, intentFunction.flutterServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
        } else {
            PendingIntent.getService(
                this, 0, intentFunction.flutterServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val c: Calendar = Calendar.getInstance()
        if (sharedPref.timerType) {
            Log.d("AlarmService", "Using timerType")
            c.apply {
                timeInMillis = System.currentTimeMillis()
                val setHour = get(Calendar.HOUR_OF_DAY) + sharedPref.relativeHour
                val setMinute = get(Calendar.MINUTE) + sharedPref.relativeMinute
                val setSecond = get(Calendar.SECOND) + sharedPref.relativeSecond

                set(Calendar.HOUR_OF_DAY, setHour)
                set(Calendar.MINUTE, setMinute)
                set(Calendar.SECOND, setSecond)
            }
        } else {
            Log.d("AlarmService", "Using alarmType")
            c.apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, sharedPref.specificHour)
                set(Calendar.MINUTE, sharedPref.specificMinute)
            }
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
                    flutterServicePendingIntent
                ),
                flutterServicePendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis,
                flutterServicePendingIntent
            )
        }
    }

    override fun onDestroy() {
        alarmManager.cancel(flutterServicePendingIntent)
        Log.d("Destroy", "AlarmService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}