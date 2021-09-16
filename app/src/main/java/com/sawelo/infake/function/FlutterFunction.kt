package com.sawelo.infake.function

import android.content.Context
import android.util.Log
import com.sawelo.infake.dataClass.ContactData
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

    /**
     * This function will only run if flutterEngine is null
     * Otherwise, use existing flutterEngineCache
     */
    fun createFlutterEngine() {
        if (flutterEngine == null) {
            Log.d("FlutterFunction", "Flutter Engine is null")

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

            getCancelMethodFromFlutter()

            Log.d("FlutterFunction", "Flutter Engine is created")
        } else
            Log.d("FlutterFunction", "Flutter Engine is not null")
    }

    /**
     * This should only be used within CallActivity to avoid redundant calls
     * Through this function, user ContactData will be sent to Flutter to be processed
     */
    fun sendContactToFlutter(contactData: ContactData) {
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

    /**
     * With this function, Android will be able to prepare for cancel method call from Flutter
     * If called, intentFunction.cancelMethod will be called to stop all services except
     * flutterEngine, therefore stopping notification from running
     */
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