package com.sawelo.infake.function

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sawelo.infake.service.AlarmService
import com.sawelo.infake.service.DeclineReceiver
import com.sawelo.infake.service.FlutterService
import com.sawelo.infake.service.NotificationService

class IntentFunction (context: Context) {

    companion object {
        const val FLUTTER_RECEIVER_ACTION = "com.sawelo.action.FLUTTER_START"
    }

    private val mContext = context

    val notificationManager: NotificationManager =
        mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val flutterServiceIntent = Intent(mContext, FlutterService::class.java)
    val notificationServiceIntent = Intent(mContext, NotificationService::class.java)
    val alarmServiceIntent = Intent(mContext, AlarmService::class.java)

    fun callDeclineService(requestCode: Int): PendingIntent {
        val declineIntent = Intent(mContext, DeclineReceiver::class.java)
        return PendingIntent.getBroadcast(
            mContext, requestCode, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun cancelCall(
        destroyFlutterEngine: Boolean = true,
        destroyAlarmService: Boolean = false) {

        Log.d("IntentFunction", "Starting cancelCall")

        notificationManager.cancel(AlarmService.NOTIFICATION_ID)
        notificationManager.cancel(NotificationService.NOTIFICATION_ID)

        mContext.stopService(notificationServiceIntent)
        mContext.stopService(flutterServiceIntent)
        if (destroyAlarmService) { mContext.stopService(alarmServiceIntent) }
        if (destroyFlutterEngine) {FlutterFunction().destroyFlutterEngine()}
    }

}