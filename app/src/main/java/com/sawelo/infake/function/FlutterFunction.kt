package com.sawelo.infake.function

import android.content.Context
import android.util.Log
import com.sawelo.infake.ContactData
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class FlutterFunction (context: Context) {
    companion object {
        private var flutterEngine:FlutterEngine? = null
        const val PLATFORM_CHANNEL = "platform_channel"
//        fun isFlutterEngineInitialized() = Companion::flutterEngine.isInitialized
    }

    private val mContext = context
    private val intentFunction = IntentFunction(mContext)

    fun createFlutterEngine() {
        // Instantiate a FlutterEngine
        flutterEngine = FlutterEngine(mContext)

        // Start executing Dart code to pre-warm the FlutterEngine
        flutterEngine?.dartExecutor?.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine
        FlutterEngineCache
            .getInstance()
            .put("engine_id", flutterEngine)

        if (flutterEngine != null) {
            Log.d("FlutterFunction", "Flutter Engine is initialized")
        } else
            Log.d("FlutterFunction", "Flutter Engine is not initialized")

        getCancelMethodFromFlutter()
    }

    fun sendContactToFlutter(contactData: ContactData) {
        /**
         * This should only be used within CallActivity to avoid redundant calls
         */

        val arguments = mutableMapOf<String, String>()
        arguments["name"] = contactData.name
        arguments["number"] = contactData.number
        arguments["route"] = contactData.route
        arguments["imageBase64"] = contactData.imageBase64

        Log.d("FlutterFunction", "$arguments")

        if (flutterEngine != null) {
            MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, PLATFORM_CHANNEL)
                .invokeMethod("get_contact", arguments)
            Log.d("FlutterFunction", "sendContactToFlutter succeed")
        } else {
            Log.d("FlutterFunction", "sendContactToFlutter failed")
        }
    }

    private fun getCancelMethodFromFlutter() {
        if (flutterEngine != null) {
            MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, PLATFORM_CHANNEL)
                .setMethodCallHandler {
                    call, result ->
                    if (call.method == "start_cancel_method") {
                        intentFunction.cancelMethod(destroyFlutterEngine = false)
                    }
                }
            Log.d("FlutterFunction", "getCancelMethodFromFlutter succeed")
        } else {
            Log.d("FlutterFunction", "getCancelMethodFromFlutter failed")
        }
    }

    fun destroyFlutterEngine() {
        if (flutterEngine != null) {
            flutterEngine?.destroy()
            flutterEngine = null
            Log.d("FlutterFunction", "FlutterEngine destroyed")
        } else {
            Log.d("FlutterFunction", "FlutterEngine is not initialized")
        }
    }
}