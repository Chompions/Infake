package com.sawelo.infake.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sawelo.infake.BuildConfig
import com.sawelo.infake.DeclineReceiver
import com.sawelo.infake.R
import com.sawelo.infake.activity.CallActivity
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

        // Create RemoteViews with custom layout
        val customNotification = RemoteViews(
            BuildConfig.APPLICATION_ID, R.layout.notification_whats_app_call
        )

        // Initialize Intents
        val defaultIntent = Intent(this, CallActivity::class.java)
                .putExtra("route", "defaultIntent")
        val answerIntent = Intent(this, CallActivity::class.java)
                .putExtra("route", "answerIntent")
        val declineIntent = Intent(this, DeclineReceiver::class.java)

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

        /** Create notificationManager to ensure functionality */
        val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)

            // Set sound for channel
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), audioAttributes)
            channel.enableVibration(true)

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
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

        val buildNotification: Notification = builder.build()
        buildNotification.flags = Notification.FLAG_INSISTENT

        // Countdown until NotificationService stops
        stopTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsUntilFinished = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                Log.d("NotificationService", "Countdown: $secondsUntilFinished")
            }
            override fun onFinish() {
                stopSelf()
            }
        }

        stopTimer.start()
        startForeground(NOTIFICATION_ID, buildNotification)
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer.cancel()
        println("NotificationService is destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}