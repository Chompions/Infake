package com.sawelo.infake.function

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sawelo.infake.service.AlarmService
import com.sawelo.infake.service.DeclineBroadcast
import com.sawelo.infake.service.FlutterStartUpService
import com.sawelo.infake.service.NotificationService

class IntentFunction (context: Context) {

    private val mContext = context

    val notificationManager: NotificationManager =
        mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val flutterStartUpServiceIntent = Intent(mContext, FlutterStartUpService::class.java)
    val notificationServiceIntent = Intent(mContext, NotificationService::class.java)
    private val alarmServiceIntent = Intent(mContext, AlarmService::class.java)

    fun callDeclineService(requestCode: Int): PendingIntent {
        println(requestCode)
        val declineIntent = Intent(mContext, DeclineBroadcast::class.java)
        return PendingIntent.getBroadcast(
            mContext, requestCode, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun cancelCall(destroyFlutterEngine: Boolean = true, destroyAlarmService: Boolean = false) {
        notificationManager.cancel(AlarmService.NOTIFICATION_ID)
        notificationManager.cancel(NotificationService.NOTIFICATION_ID)

        mContext.stopService(notificationServiceIntent)
        mContext.stopService(flutterStartUpServiceIntent)
        if (destroyAlarmService) { mContext.stopService(alarmServiceIntent) }
        if (destroyFlutterEngine) {FlutterFunction().destroyFlutterEngine()}
    }

}