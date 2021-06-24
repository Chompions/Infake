package com.sawelo.infake

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateViewModel: ViewModel() {
    private val _scheduleText = MutableLiveData<String>()
    val scheduleText: LiveData<String> = _scheduleText

    init {
        _scheduleText.value = "Schedule Call"
    }

    fun updateScheduleText(hour: Int, minute: Int) {
        val doubleDigitMinute: String = if (minute < 10) {"0$minute"} else {"$minute"}
        val newValue = "$hour:$doubleDigitMinute"
        _scheduleText.value = newValue
        Log.d("CreateViewModel", "Update: $newValue")
    }
}