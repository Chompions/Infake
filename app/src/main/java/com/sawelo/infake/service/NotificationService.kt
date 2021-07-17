package com.sawelo.infake.service

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.sawelo.infake.BuildConfig
import com.sawelo.infake.R
import com.sawelo.infake.activity.CallActivity
import com.sawelo.infake.function.IntentFunction
import java.util.concurrent.TimeUnit


class NotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "call_id"
        const val CHANNEL_NAME = "Call Channel"
        const val NOTIFICATION_ID = 2
    }

    private lateinit var stopTimer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("NotificationService", "Starting NotificationService")

        val intentFunction = IntentFunction(this)

        // Create RemoteViews with custom layout
        val customNotification = RemoteViews(
            BuildConfig.APPLICATION_ID, R.layout.notification_whats_app_call
        )

        // Initialize Intents
        val defaultIntent = Intent(this, CallActivity::class.java)
                .putExtra("route", "defaultIntent")
        val answerIntent = Intent(this, CallActivity::class.java)
                .putExtra("route", "answerIntent")
        val declineIntent = Intent(this, DeclineBroadcast::class.java)

        // Initialize PendingIntents
        val defaultPendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 1, defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val answerPendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 2, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val declinePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this, 3, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Applying PendingIntents on buttons in customNotification
        customNotification.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
        customNotification.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH).apply {

            }

            // Set sound for channel
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            channel.apply {
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), audioAttributes)
                enableVibration(true)
            }

            // Register the channel with the system
            intentFunction.notificationManager.createNotificationChannel(channel)
        }

        // Build Notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(defaultPendingIntent, true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(customNotification)
                .setCustomBigContentView(customNotification)
                .setOngoing(true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 250, 1000, 250, 1000))

        val buildNotification: Notification = builder.build().apply {
            this.flags = Notification.FLAG_INSISTENT
        }

        // Countdown until NotificationService stops
        stopTimer = object : CountDownTimer(25000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                Log.d("NotificationService", "Countdown: $secondsUntilFinished")
            }
            override fun onFinish() {
                intentFunction.cancelCall(destroyAlarmService = true)
            }
        }

        stopTimer.start()
        intentFunction.notificationManager.notify(NOTIFICATION_ID, buildNotification)
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer.cancel()
        Log.d("Destroy", "NotificationService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}