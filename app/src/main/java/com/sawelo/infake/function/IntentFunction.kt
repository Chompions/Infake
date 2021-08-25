package com.sawelo.infake.function

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sawelo.infake.service.AlarmService
import com.sawelo.infake.service.DeclineReceiver
import com.sawelo.infake.service.FlutterService
import com.sawelo.infake.service.NotificationService

class IntentFunction (context: Context) {

    private val mContext = context

    val notificationManager: NotificationManager =
        mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val flutterServiceIntent = Intent(mContext, FlutterService::class.java)
    val notificationServiceIntent = Intent(mContext, NotificationService::class.java)
    val alarmServiceIntent = Intent(mContext, AlarmService::class.java)

    @SuppressLint("UnspecifiedImmutableFlag")
    fun callDeclineService(requestCode: Int): PendingIntent {
        val declineIntent = Intent(mContext, DeclineReceiver::class.java)
        return if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.getBroadcast(
                mContext, requestCode, declineIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                mContext, requestCode, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    }

    fun cancelMethod(
        destroyFlutterEngine: Boolean = true,
        destroyAlarmService: Boolean = false) {

        Log.d("IntentFunction", "Starting cancelCall")

        notificationManager.cancelAll()

        mContext.stopService(notificationServiceIntent)
        mContext.stopService(flutterServiceIntent)

        if (FlutterService.stopTimer != null) {
            FlutterService.stopTimer!!.cancel()
            FlutterService.stopTimer = null
        }

        if (destroyAlarmService) { mContext.stopService(alarmServiceIntent) }
        if (destroyFlutterEngine) {FlutterFunction(mContext).destroyFlutterEngine()}
    }

}