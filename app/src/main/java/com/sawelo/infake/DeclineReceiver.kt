package com.sawelo.infake

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sawelo.infake.function.FlutterFunction

class DeclineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.stopService(Intent(context, NotificationService::class.java))
        FlutterFunction().destroyFlutterEngine()
    }
}