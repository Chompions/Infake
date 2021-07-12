package com.sawelo.infake

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CreateViewModel: ViewModel() {
    private val _scheduleText = MutableLiveData<String>()
    val scheduleText: LiveData<String> = _scheduleText

    init {
        _scheduleText.value = "Schedule Call"
    }

    fun updateScheduleText(hour: Int, minute: Int) {
        val c: Calendar = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)
        val currentMinuteOfDay = (currentHour*60)+currentMinute
        Log.d("CreateViewModel", "currentMinuteOfDay is $currentMinuteOfDay")

        val assignedMinuteOfDay = (hour*60)+minute
        Log.d("CreateViewModel", "assignedMinuteOfDay is $assignedMinuteOfDay")


        val newValue = if (assignedMinuteOfDay <= currentMinuteOfDay) {
            "Now"
        } else {
            val doubleDigitMinute: String =
                if (minute < 10) {"0$minute"} else {"$minute"}
            "$hour:$doubleDigitMinute"
        }

        _scheduleText.value = newValue
        Log.d("CreateViewModel", "Update: $newValue")
    }
}