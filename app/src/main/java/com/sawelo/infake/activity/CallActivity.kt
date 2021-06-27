package com.sawelo.infake.activity

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sawelo.infake.ContactData
import com.sawelo.infake.R
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.service.NotificationService
import io.flutter.embedding.android.FlutterFragment

class CallActivity: FragmentActivity(), SensorEventListener {

    companion object {
        private const val TAG_FLUTTER_FRAGMENT = "flutter_fragment"
        private const val SENSOR_SENSITIVITY = 4
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var powerManager: PowerManager
    private lateinit var fragmentManager: FragmentManager
    private lateinit var partialWakeLock: PowerManager.WakeLock
    private lateinit var proximityWakeLock: PowerManager.WakeLock

    private var flutterFragment: FlutterFragment? = null
    private var mProximity: Sensor? = null
    private var useProximityWakeLock: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Stop NotificationService
        this.stopService(Intent(this, NotificationService::class.java))
        Log.d("CallActivity", "Service stopped")

        // Initialize wakeLock & variables
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        setWakeLock()
        if (!useProximityWakeLock) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            mProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        }

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
        fragmentManager = supportFragmentManager

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
                .add(R.id.activity_call_fragment_container, newFlutterFragment, TAG_FLUTTER_FRAGMENT)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!useProximityWakeLock) {
            sensorManager.registerListener(this, mProximity, 100000)
        }
    }
    override fun onPause() {
        super.onPause()
        if (!useProximityWakeLock) {
            sensorManager.unregisterListener(this)
        }
    }

    private fun setWakeLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "infake:proximity_wake_lock")
            proximityWakeLock.acquire(10*60*1000L)
            useProximityWakeLock = true
        }
         else {
            partialWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "infake:partial_wake_lock")
            useProximityWakeLock = false
        }
    }

    @Suppress("DEPRECATION")
    fun hideSystemUI() {
        if (!partialWakeLock.isHeld) {
            partialWakeLock.acquire(10*60*1000L)
        }
        window.decorView.systemUiVisibility = (
                // Hide the nav bar and status bar
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    @Suppress("DEPRECATION")
    fun showSystemUI() {
        if (partialWakeLock.isHeld) {
            partialWakeLock.release()
        }
        window.decorView.systemUiVisibility = 0
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!useProximityWakeLock) {
            if (event != null && event.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                    // Near
                    Log.d("onSensorChanged", "It's near")
                    if (flutterFragment != null && flutterFragment!!.isVisible) {
                        hideSystemUI()
                        fragmentManager
                            .beginTransaction()
                            .hide(flutterFragment!!)
                            .commitNow()
                    }
                } else {
                    // Far
                    Log.d("onSensorChanged", "It's far")
                    if (flutterFragment != null && !flutterFragment!!.isVisible) {
                        showSystemUI()
                        fragmentManager
                            .beginTransaction()
                            .show(flutterFragment!!)
                            .commitNow()
                    }
                    Log.d("onSensorChanged",
                        "currentBrightness is ${this.window.attributes.screenBrightness}")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        FlutterFunction().destroyFlutterEngine()
    }
}