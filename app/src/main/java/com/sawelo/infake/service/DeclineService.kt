package com.sawelo.infake.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.sawelo.infake.function.IntentFunction

class DeclineService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /** Cancel and destroy everything from this Broadcast Receiver */
        IntentFunction(this).cancelCall(destroyAlarmService = true)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}