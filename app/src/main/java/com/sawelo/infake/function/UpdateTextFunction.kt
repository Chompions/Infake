package com.sawelo.infake.function

import android.content.Context
import android.util.Log
import com.sawelo.infake.ScheduleData
import java.util.*

class UpdateTextFunction(context: Context) {

    private val mContext = context

    fun updateMainText(
        scheduleData: ScheduleData
    ):
            Pair<String, String> {

        /**
         *  This function was made for CreateViewModel and AlarmService
         *  It will run while user adjust the alarm settings and moments before createProfile starts
         *  Result will vary according to scheduleData.timerType to adjust input time type
         *
         */

        var mainScheduleText = ""
        var notificationText = ""
        val displayText: String

        val sharedPref = SharedPrefFunction(mContext)

        // Get current time from phone
        val c: Calendar = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)

        // Get time data from constructor
        val dataHour = scheduleData.hour ?: 0
        val dataMinute = scheduleData.minute ?: 0
        val dataSecond = scheduleData.second ?: 0

        fun adjustNum(numValue: Int?, singular: String, plural: String): String {
            return when (numValue) {
                0 -> ""
                1 -> "1 $singular"
                else -> "$numValue $plural"
            }
        }

        when (scheduleData.timerType) {
            // This will return text for timer type
            true -> {
                // Get incoming call exact starting time
                val setHour = currentHour + dataHour
                val setMinute = currentMinute + dataMinute

                val textHour: String = adjustNum(dataHour, "hour", "hours")
                val textMinute = adjustNum(dataMinute, "minute", "minutes")
                val textSecond = adjustNum(dataSecond, "second", "seconds")

                // Checks if all val is not blank
                displayText =
                    if (textHour.isNotBlank() && textMinute.isNotBlank() && textSecond.isNotBlank()) {
                        // Use already existing data with updateRelativeTime() in digits
                        // (ex. 00:00:00)
                        val hourPad: String = sharedPref.relativeHour.toString().padStart(2, '0')
                        val minutePad: String = sharedPref.relativeMinute.toString().padStart(2, '0')
                        val secondPad: String = sharedPref.relativeSecond.toString().padStart(2, '0')
                        "$hourPad:$minutePad:$secondPad"
                    } else {
                        val builder = StringBuilder()
                        if (textHour.isNotBlank()) builder.append(" ").append(textHour)
                        if (textMinute.isNotBlank()) builder.append(" ").append(textMinute)
                        if (textSecond.isNotBlank()) builder.append(" ").append(textSecond)
                        builder.toString()
                    }

                val hourPad: String = setHour.toString().padStart(2, '0')
                val minutePad: String = setMinute.toString().padStart(2, '0')

                // Set now or else
                if (scheduleData.hour == 0 && scheduleData.minute == 0 && scheduleData.second == 0) {
                    mainScheduleText = "Now"
                    notificationText = "The call is starting now"
                } else {
                    mainScheduleText = displayText
                    notificationText = "Preparing call for$displayText ($hourPad:$minutePad)"
                }

            }
            // This will return text for alarm type
            false -> {
                val hourPad: String = dataHour.toString().padStart(2, '0')
                val minutePad: String = dataMinute.toString().padStart(2, '0')

                val dataMinuteOfDay = (dataHour * 60) + dataMinute
                val currentMinuteOfDay = (currentHour * 60) + currentMinute
                val minuteOfDay = dataMinuteOfDay - currentMinuteOfDay

                val textHour: String = adjustNum(minuteOfDay / 60, "hour", "hours")
                val textMinute = adjustNum(minuteOfDay % 60, "minute", "minutes")

                val builder = StringBuilder()
                if (textHour.isNotBlank()) builder.append(" ").append(textHour)
                if (textMinute.isNotBlank()) builder.append(" ").append(textMinute)
                displayText = builder.toString()

                // Set now or else
                if (dataMinuteOfDay <= currentMinuteOfDay) {
                    mainScheduleText = "Now"
                    notificationText = "The call is starting now"
                } else {
                    mainScheduleText = "$hourPad:$minutePad"
                    notificationText = "Preparing call for$displayText ($hourPad:$minutePad)"
                }
            }
        }
        Log.d("UpdateTextFunction", "mainScheduleText is $mainScheduleText")
        Log.d("UpdateTextFunction", "notificationText is $notificationText")

        return Pair(mainScheduleText, notificationText)
    }
}