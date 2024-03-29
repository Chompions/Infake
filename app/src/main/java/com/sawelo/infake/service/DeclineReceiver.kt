package com.sawelo.infake.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sawelo.infake.function.IntentFunction

class DeclineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DeclineReceiver", "Starting DeclineReceiver")
        IntentFunction(context).cancelMethod(
            destroyAlarmService = true)
    }
}