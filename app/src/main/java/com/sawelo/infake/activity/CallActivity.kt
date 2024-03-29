package com.sawelo.infake.activity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sawelo.infake.R
import com.sawelo.infake.`object`.StaticObject
import com.sawelo.infake.dataClass.ContactData
import com.sawelo.infake.function.FlutterFunction
import com.sawelo.infake.function.IntentFunction
import com.sawelo.infake.function.SharedPrefFunction
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
    private lateinit var intentFunction: IntentFunction

    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var partialWakeLock: PowerManager.WakeLock? = null

    private var flutterFragment: FlutterFragment? = null
    private var mProximity: Sensor? = null
    private var useProximityWakeLock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        intentFunction = IntentFunction(this)

        // Stop everything except flutterEngine if CallActivity starts by pressing notification
        val action = intent.action
        if (action == NotificationService.INTENT_ACTION) {
            intentFunction.cancelMethod(destroyFlutterEngine = false)
        }

        // Setting up display switch (on/off) during call
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        setWakeLock()
        if (!useProximityWakeLock) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            mProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        }

        // Instantiate sharedPref
        val sharedPref = SharedPrefFunction(this)

        fun initializeMethodCall(routeExtra: String) {
            FlutterFunction(this).sendContactToFlutter(
                ContactData(
                    sharedPref.activeName,
                    sharedPref.activeNumber,
                    routeExtra,
                    sharedPref.imageBase64
                )
            )
            Log.d("CallActivity", "name: ${sharedPref.activeName}")
            Log.d("CallActivity", "number: ${sharedPref.activeNumber}")
            Log.d("CallActivity", "routeExtra: $routeExtra")
            Log.d("CallActivity", "imageBase64: ${sharedPref.imageBase64}")
        }

        val route = intent.extras?.getString("route")
        val incomingRouteName = sharedPref.activeIncomingRouteName
        val ongoingRouteName = StaticObject.screenRouteList.single {
            it.incomingRouteName == incomingRouteName
        }.ongoingRouteName

        if (route != null) {
            when (route) {
                "defaultIntent" -> initializeMethodCall(incomingRouteName)
                "answerIntent" -> initializeMethodCall(ongoingRouteName)
            }
        }

        // Ensure CallActivity will run when the phone is locked or the screen is off
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setTranslucent(true)
            }
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

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
        // Set wakeLock according to phone's OS version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /**
             * Proximity wakeLock is currently the best way to turn screen off from close proximity
             * However this wakeLock can only work for buildVersion above Lollipop
             * */
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "infake::proximity_wake_lock")
            proximityWakeLock?.acquire(10*60*1000L)
            useProximityWakeLock = true
        } else {
            /**
             * Partial wakeLock will require additional workaround to simulate turning off screen
             *
             * This wakeLock will be accompanied by showSystemUI(), hideSystemUI(),
             * onSensorChanged() with sensor of TYPE_PROXIMITY, and additionally sensor listeners
             * in onResume() and onPause()
             * */
            partialWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "infake::partial_wake_lock")
            useProximityWakeLock = false
        }
    }

    @Suppress("DEPRECATION")
    fun hideSystemUI() {
        if (partialWakeLock?.isHeld == false && partialWakeLock != null) {
            partialWakeLock?.acquire(10*60*1000L)
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
        if (partialWakeLock?.isHeld == true && partialWakeLock != null) {
            partialWakeLock?.release()
        }
        window.decorView.systemUiVisibility = 0
    }

    override fun onSensorChanged(event: SensorEvent?) {
        /**
         * onSensorChanged will only run if partialWakeLock is running and
         * proximityWakeLock is not in use
         *
         * If proximity declares object is near -> then hideSystemUI, closes flutterFragment and
         * potentially turn screen off after a certain duration
         *
         * Otherwise, if proximity declares object is far -> then showSystemUI and reveals
         * flutterFragment
         * */

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
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!useProximityWakeLock) { sensorManager.unregisterListener(this) }
        if (proximityWakeLock?.isHeld == true) {proximityWakeLock?.release()}
        if (partialWakeLock?.isHeld == true) {partialWakeLock?.release()}
        intentFunction.cancelMethod(destroyAlarmService = true)
        Log.d("Destroy", "CallActivity is destroyed")
    }
}