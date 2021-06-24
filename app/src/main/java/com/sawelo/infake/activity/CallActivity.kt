package com.sawelo.infake.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.*
import com.sawelo.infake.NotificationService
import com.sawelo.infake.ContactData
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.R
import io.flutter.embedding.android.FlutterFragment

class CallActivity: FragmentActivity() {
    companion object {
        private const val TAG_FLUTTER_FRAGMENT = "flutter_fragment"
    }
    private var flutterFragment: FlutterFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Stop NotificationService
        this.stopService(Intent(this, NotificationService::class.java))
        Log.d("CallActivity", "Service stopped")

        // Instantiate sharedPref
        val sharedPref = applicationContext.getSharedPreferences(
                "PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val activeName = sharedPref.getString("ACTIVE_NAME", "Default Name")
        val activeNumber = sharedPref.getString("ACTIVE_NUMBER", "Default Number")
        Log.d("CallActivity", "Data: $activeName, $activeNumber")

        fun initializeMethodCall(routeExtra: String) {
            FlutterFunction().sendMethodCall(
                ContactData(
                    activeName!!,
                    activeNumber!!,
                    routeExtra,
                ))
            Log.d("CallActivity", "routeExtra: $routeExtra")
        }

        val route = intent.extras?.getString("route")
        if (route != null) {
            when (route) {
                "defaultIntent" -> initializeMethodCall("/WhatsAppIncomingCall")
                "answerIntent" -> initializeMethodCall("/WhatsAppOngoingCall")
            }
        }
        Log.d("CallActivity", "intent: $route")

        // Get Activity's FragmentManager
        val fragmentManager: FragmentManager = supportFragmentManager

        // Find existing FlutterFragment if existed
        flutterFragment = fragmentManager
            .findFragmentByTag(TAG_FLUTTER_FRAGMENT) as FlutterFragment?

        // Create and attach a FlutterFragment if one does not exist
        if (flutterFragment == null) {
            val newFlutterFragment = FlutterFragment
                .withCachedEngine("engine_id")
                .build<FlutterFragment>()
            flutterFragment = newFlutterFragment
            fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, newFlutterFragment, TAG_FLUTTER_FRAGMENT)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FlutterFunction().destroyFlutterEngine()
    }
}