package com.sawelo.infake.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private lateinit var flutterReceiverPendingIntent: PendingIntent
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var intentFunction: IntentFunction
    private lateinit var flutterReceiver: FlutterReceiver
    private lateinit var sharedPref: SharedPrefFunction

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("AlarmService", "Starting AlarmService")

        sharedPref = SharedPrefFunction(this)
        intentFunction = IntentFunction(this)

        flutterReceiver = FlutterReceiver()
        val flutterReceiverFilter = IntentFilter().apply {
            addAction(IntentFunction.FLUTTER_RECEIVER_ACTION)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        registerReceiver(flutterReceiver, flutterReceiverFilter)

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
                .updateMainText(sharedPref.scheduleData, false)

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

        // Cancel everything except receiver before starting alarm
        intentFunction.cancelCall()
        startForeground(NOTIFICATION_ID, builder.build())
        return START_STICKY
    }

    // Setting RCT_Wakeup directly to FlutterReceiver
    private fun setAlarm() {
        Log.d("AlarmService", "Run setAlarm() for ${sharedPref.scheduleText}")

        // Create Intent & PendingIntent to start FlutterReceiver
        flutterReceiverPendingIntent = PendingIntent.getBroadcast(
            this, 0, intentFunction.flutterReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Create AlarmManager
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
                    flutterReceiverPendingIntent
                ),
                flutterReceiverPendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis,
                flutterReceiverPendingIntent
            )
        }
    }

    override fun onDestroy() {
        unregisterReceiver(flutterReceiver)
        if (FlutterReceiver.timerStarted) {
            FlutterReceiver.stopTimer.cancel()
            FlutterReceiver.timerStarted = false
        }
        alarmManager.cancel(flutterReceiverPendingIntent)

        Log.d("Destroy", "AlarmService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}