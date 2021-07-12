package com.sawelo.infake.function

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.sawelo.infake.ContactData
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class FlutterFunction {
    companion object {
        private var flutterEngine:FlutterEngine? = null
//        fun isFlutterEngineInitialized() = Companion::flutterEngine.isInitialized
    }

    fun createFlutterEngine(context: Context) {
        // Instantiate a FlutterEngine
        flutterEngine = FlutterEngine(context)

        // Start executing Dart code to pre-warm the FlutterEngine
        flutterEngine?.dartExecutor?.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        val sharedPref = SharedPrefFunction(context)
        sendMethodCall(
            ContactData(
                sharedPref.activeName,
                sharedPref.activeNumber,
                sharedPref.activeRoute,
            )
        )

        // Cache the FlutterEngine
        FlutterEngineCache
            .getInstance()
            .put("engine_id", flutterEngine)

        if (flutterEngine != null) {
            Log.d("FlutterFunction", "Flutter Engine is initialized")
        } else
            Log.d("FlutterFunction", "Flutter Engine is not initialized")

    }

    fun sendMethodCall(contactData: ContactData) {
        // This should only be used within CallActivity and FlutterFunction
        // to avoid redundant calls

        if (flutterEngine != null) {
            MethodChannel(
                flutterEngine?.dartExecutor?.binaryMessenger, "method_channel_name"
            )
                .invokeMethod(
                    "call_method",
                    Gson().toJson(contactData)
                )
        } else {
            Log.d("FlutterFunction", "sendMethodCall failed")
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