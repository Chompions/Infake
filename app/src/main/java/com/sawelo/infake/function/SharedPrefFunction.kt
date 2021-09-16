package com.sawelo.infake.function

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.sawelo.infake.dataClass.ScheduleData

class SharedPrefFunction(context: Context) {
    companion object {
        const val ACTIVE_NAME = "ACTIVE_NAME"
        const val ACTIVE_NUMBER = "ACTIVE_NUMBER"
        const val ACTIVE_ROUTE = "ACTIVE_ROUTE"
        const val IMAGE_BASE_64 = "IMAGE_BASE_64"
        const val TEMP_IMAGE_BASE_64 = "TEMP_IMAGE_BASE_64"

        const val SPECIFIC_HOUR = "SPECIFIC_HOUR"
        const val SPECIFIC_MINUTE = "SPECIFIC_MINUTE"

        const val RELATIVE_HOUR = "RELATIVE_HOUR"
        const val RELATIVE_MINUTE = "RELATIVE_MINUTE"
        const val RELATIVE_SECOND = "RELATIVE_SECOND"

        const val TIMER_TYPE = "TIMER_TYPE"
    }

    private val bitmapFunction = BitmapFunction(context)
    private val sharedPref: SharedPreferences = context.getSharedPreferences(
            "PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)

    val editor: SharedPreferences.Editor = sharedPref.edit()

    // Retrieve data from sharedPref
    val activeName= sharedPref.getString(ACTIVE_NAME, "Citra") ?: "Citra"
    val activeNumber = sharedPref.getString(ACTIVE_NUMBER, "0123456789") ?: "0123456789"

    val activeIncomingRouteName = sharedPref.getString(ACTIVE_ROUTE, "/InitialRoute") ?: "/InitialRoute"

    val imageBase64 = sharedPref.getString(IMAGE_BASE_64, bitmapFunction.returnDefault()) ?: bitmapFunction.returnDefault()
    val tempImageBase64 = sharedPref.getString(TEMP_IMAGE_BASE_64, bitmapFunction.returnDefault()) ?: bitmapFunction.returnDefault()

    val specificHour = sharedPref.getInt(SPECIFIC_HOUR, 0)
    val specificMinute = sharedPref.getInt(SPECIFIC_MINUTE, 0)

    val relativeHour = sharedPref.getInt(RELATIVE_HOUR, 0)
    val relativeMinute = sharedPref.getInt(RELATIVE_MINUTE, 0)
    val relativeSecond = sharedPref.getInt(RELATIVE_SECOND, 0)

    val timerType = sharedPref.getBoolean(TIMER_TYPE, true)

    fun scheduleData(): ScheduleData {
        Log.d("SharedPrefFunction", "timerType is $timerType")
        return if (timerType) {
            ScheduleData(
                true,
                relativeHour = relativeHour,
                relativeMinute = relativeMinute,
                relativeSecond = relativeSecond)
        } else {
            ScheduleData(false,
                specificHour = specificHour,
                specificMinute = specificMinute)
        }
    }
}