package com.sawelo.infake.`object`

import android.util.Log
import com.sawelo.infake.dataClass.ScheduleData
import java.util.*

object UpdateTextObject {

    /**
     *  This function will return string for mainScheduleText in CreateViewModel
     *  and notificationText in AlarmService respectively
     *
     *  It will run while user adjust the alarm settings and moments before createProfile starts
     *  Result will vary according to scheduleData.timerType to adjust input time type
     */
    fun updateMainText(
        scheduleData: ScheduleData
    ): Pair<String, String> {
        var mainScheduleText = ""
        var notificationText = ""
        val displayText: String

        // Get current time from phone
        val c: Calendar = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)

        // Get time data from constructor
        val relativeHour = scheduleData.relativeHour
        val relativeMinute = scheduleData.relativeMinute
        val relativeSecond = scheduleData.relativeSecond

        val specificHour = scheduleData.specificHour
        val specificMinute = scheduleData.specificMinute

        /**
         * Adjust number to return singular or plural suffix.
         * Example: 1 hour; 2 hours
         */
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
                /**
                 * Setting setHour and setMinute according to input, also limiting upper limit to
                 * 24 hours and 60 minutes each. If setMinute goes beyond upper limit, then the rest
                 * will be added to setHour.
                 */

                // Get incoming call exact starting time
                var setHour = (currentHour + relativeHour) % 24
                var setMinute = (currentMinute + relativeMinute)
                while (setMinute > 60) {
                    setHour++
                    setMinute %= 60
                }

                // Set padding for setHour & setMinute
                val hourPad: String = setHour.toString().padStart(2, '0')
                val minutePad: String = setMinute.toString().padStart(2, '0')

                val textHour: String = adjustNum(relativeHour, "hour", "hours")
                val textMinute = adjustNum(relativeMinute, "minute", "minutes")
                val textSecond = adjustNum(relativeSecond, "second", "seconds")

                // Checks if all val is not blank
                displayText =
                    if (textHour.isNotBlank() && textMinute.isNotBlank() && textSecond.isNotBlank()) {
                        // Use already existing data with updateRelativeTime() in digits
                        // (example text format = 00:00:00)
                        val hourPadToDisplay: String = relativeHour.toString().padStart(2, '0')
                        val minutePadToDisplay: String =
                            relativeMinute.toString().padStart(2, '0')
                        val secondPadToDisplay: String =
                            relativeSecond.toString().padStart(2, '0')
                        " $hourPadToDisplay:$minutePadToDisplay:$secondPadToDisplay"
                    } else {
                        // Otherwise use sets of string to represent time
                        // (example text format = 2 hours 3 minutes)
                        val builder = StringBuilder()
                        if (textHour.isNotBlank()) builder.append(" ").append(textHour)
                        if (textMinute.isNotBlank()) builder.append(" ").append(textMinute)
                        if (textSecond.isNotBlank()) builder.append(" ").append(textSecond)
                        builder.toString()
                    }

                // Set now or else
                if (relativeHour == 0 && relativeMinute == 0 && relativeSecond == 0) {
                    mainScheduleText = "Now"
                    notificationText = "The call is starting now"
                } else {
                    mainScheduleText = displayText
                    notificationText = "Preparing call for$displayText ($hourPad:$minutePad)"
                }

            }
            // This will return text for alarm type
            false -> {
                val hourPad: String = specificHour.toString().padStart(2, '0')
                val minutePad: String = specificMinute.toString().padStart(2, '0')

                // dataMinuteOfDay is total time from 00:00 to target time in minutes
                val dataMinuteOfDay = (specificHour * 60) + specificMinute
                println("dataMinuteDay: $dataMinuteOfDay")
                // currentMinuteOfDay is total time from 00:00 until now in minutes
                val currentMinuteOfDay = (currentHour * 60) + currentMinute
                println("currentMinuteDay: $currentMinuteOfDay")
                // minuteOfDay is total time from now to target time in minutes
                val minuteOfDay = dataMinuteOfDay - currentMinuteOfDay

                val textHour: String = adjustNum(minuteOfDay / 60, "hour", "hours")
                val textMinute = adjustNum(minuteOfDay % 60, "minute", "minutes")

                val builder = StringBuilder()
                if (textHour.isNotBlank()) builder.append(" ").append(textHour)
                if (textMinute.isNotBlank()) builder.append(" ").append(textMinute)
                displayText = builder.toString()

                if (dataMinuteOfDay <= currentMinuteOfDay) {
                    mainScheduleText = "Now"
                    notificationText = "The call is starting now"
                } else {
                    mainScheduleText = "$hourPad:$minutePad"
                    notificationText = "Preparing call for$displayText ($hourPad:$minutePad)"
                }
            }
        }
        Log.d("UpdateTextObject", "mainScheduleText is $mainScheduleText")
        Log.d("UpdateTextObject", "notificationText is $notificationText")

        return Pair(mainScheduleText, notificationText)
    }
}