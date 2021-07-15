package com.sawelo.infake.function

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sawelo.infake.service.AlarmService
import com.sawelo.infake.service.DeclineService
import com.sawelo.infake.service.FlutterStartUpService
import com.sawelo.infake.service.NotificationService

class IntentFunction (context: Context) {

    private val mContext = context

    val notificationManager: NotificationManager =
        mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val flutterStartUpServiceIntent = Intent(mContext, FlutterStartUpService::class.java)
    val notificationServiceIntent = Intent(mContext, NotificationService::class.java)
    private val alarmServiceIntent = Intent(mContext, AlarmService::class.java)

    private val declineIntent = Intent(mContext, DeclineService::class.java)
    val declinePendingIntent: PendingIntent = PendingIntent.getService(
        context, 0, declineIntent, PendingIntent.FLAG_CANCEL_CURRENT)

    fun cancelCall(destroyFlutterEngine: Boolean = true, destroyAlarmService: Boolean = false) {
        notificationManager.cancel(AlarmService.NOTIFICATION_ID)
        notificationManager.cancel(NotificationService.NOTIFICATION_ID)

        mContext.stopService(notificationServiceIntent)
        mContext.stopService(flutterStartUpServiceIntent)
        if (destroyFlutterEngine) {FlutterFunction().destroyFlutterEngine()}
        if (destroyAlarmService) { mContext.stopService(alarmServiceIntent) }
    }

}