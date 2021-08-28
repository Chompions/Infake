package com.sawelo.infake.service

import android.annotation.SuppressLint
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
import com.sawelo.infake.function.BitmapFunction
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
import java.util.concurrent.TimeUnit


class NotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "call_id"
        const val CHANNEL_NAME = "Call Channel"
        const val NOTIFICATION_ID = 2
        const val INTENT_ACTION = "answerCall"
    }

    private lateinit var stopTimer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("NotificationService", "Starting NotificationService")

        val intentFunction = IntentFunction(this)
        val sharedPref = SharedPrefFunction(this)
        val bitmapFunction = BitmapFunction(this)

        val roundedBitmap = bitmapFunction.getCircleBitmap(
            bitmapFunction.convertBase64ToBitmap(sharedPref.imageBase64)
        )

        // Create RemoteViews with custom layout
        val customNotificationLayout = RemoteViews(
            BuildConfig.APPLICATION_ID, R.layout.notification_whats_app_call).apply {
                setImageViewBitmap(R.id.notification_picture, roundedBitmap)
            setTextViewText(R.id.notification_name, sharedPref.activeName)
        }

        // Initialize Intents
        val defaultIntent = Intent(this, CallActivity::class.java)
            .putExtra("route", "defaultIntent")

        val answerIntent = Intent(this, CallActivity::class.java)
            .putExtra("route", "answerIntent")
            .setAction(INTENT_ACTION)

        val declineIntent = Intent(this, DeclineReceiver::class.java)

        val defaultPendingIntent: PendingIntent?
        val answerPendingIntent: PendingIntent?
        val declinePendingIntent: PendingIntent?

        // Initialize PendingIntents for API level 23 or else
        @SuppressLint("UnspecifiedImmutableFlag")
        if (Build.VERSION.SDK_INT >= 23) {
            defaultPendingIntent = PendingIntent.getActivity(
                this,
                1,
                defaultIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            answerPendingIntent = PendingIntent.getActivity(
                this,
                2,
                answerIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            declinePendingIntent = PendingIntent.getBroadcast(
                this,
                3,
                declineIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            defaultPendingIntent = PendingIntent.getActivity(
                this, 1, defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            answerPendingIntent = PendingIntent.getActivity(
                this, 2, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            declinePendingIntent = PendingIntent.getBroadcast(
                this, 3, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Applying PendingIntents on buttons in customNotification
        customNotificationLayout.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
        customNotificationLayout.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)

        // Create NotificationChannel only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                    audioAttributes
                )
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
            .setCustomContentView(customNotificationLayout)
            .setVisibility(VISIBILITY_PUBLIC)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))

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
                intentFunction.cancelMethod(destroyAlarmService = true)
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