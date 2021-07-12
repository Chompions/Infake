package com.sawelo.infake

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.service.NotificationService

class DeclineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationServiceIntent = Intent(context, NotificationService::class.java)

        notificationManager.cancel(NotificationService.NOTIFICATION_ID)
        context.stopService(notificationServiceIntent)
        FlutterFunction().destroyFlutterEngine()
    }
}