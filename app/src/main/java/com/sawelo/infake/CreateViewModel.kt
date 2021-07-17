package com.sawelo.infake

import android.content.Context
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.infake.databinding.DialogRelativeScheduleBinding
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*

class CreateViewModel: ViewModel() {
    private val _scheduleText = MutableLiveData<String>()
    val scheduleText: LiveData<String> = _scheduleText

    private val _relativeTimeText =MutableLiveData<String>()
    val relativeTimeText: LiveData<String> = _relativeTimeText

    private val _relativeHourNum = MutableLiveData<Int>()
    private val _relativeMinuteNum = MutableLiveData<Int>()
    private val _relativeSecondNum = MutableLiveData<Int>()

    private val _specificHourNum = MutableLiveData<Int>()
    private val _specificMinuteNum = MutableLiveData<Int>()

    private val _isTimerType = MutableLiveData<Boolean>()

    init {
        _scheduleText.value = "Schedule Call"
        _relativeTimeText.value = "Set timer"

        _relativeHourNum.value = 0
        _relativeMinuteNum.value = 0
        _relativeSecondNum.value = 0

        _specificHourNum.value = 0
        _specificMinuteNum.value = 0

        _isTimerType.value = true
    }

    fun updateRelativeTime(picker: NumberPicker?, binding: DialogRelativeScheduleBinding, newVal: Int) {
        if (picker != null) {
            when (picker.id) {
                binding.relativeTimeHour.id -> _relativeHourNum.value = newVal
                binding.relativeTimeMinute.id -> _relativeMinuteNum.value = newVal
                binding.relativeTimeSecond.id -> _relativeSecondNum.value = newVal
            }
        }
        val hourPad: String = _relativeHourNum.value.toString().padStart(2, '0')
        val minutePad: String = _relativeMinuteNum.value.toString().padStart(2, '0')
        val secondPad: String = _relativeSecondNum.value.toString().padStart(2, '0')
        if (_relativeHourNum.value == 0 &&
            _relativeMinuteNum.value == 0 &&
            _relativeSecondNum.value == 0 ) {
            _relativeTimeText.value = "Set timer"
        } else {
            _relativeTimeText.value = "$hourPad:$minutePad:$secondPad"
        }
    }

    fun setRelativeTime(context: Context) {
        val hour: Int = _relativeHourNum.value ?: 0
        val minute: Int =  _relativeMinuteNum.value ?: 0
        val second: Int = _relativeSecondNum.value ?: 0

        val sharedPref = SharedPrefFunction(context)
        with(sharedPref.editor) {
            putInt(SharedPrefFunction.RELATIVE_HOUR, hour)
            putInt(SharedPrefFunction.RELATIVE_MINUTE, minute)
            putInt(SharedPrefFunction.RELATIVE_SECOND, second)
            putBoolean(SharedPrefFunction.TIMER_TYPE, true)
            apply()
        }

        _isTimerType.value = true
        updateScheduleText(context)
    }

    fun updateSpecificTime(hourOfDay: Int, minute: Int) {
        _specificHourNum.value = hourOfDay
        _specificMinuteNum.value = minute
    }

    fun setSpecificTime(context: Context) {
        val hourOfDay: Int = _specificHourNum.value ?: 0
        val minute: Int = _specificMinuteNum.value ?: 0

        val sharedPref = SharedPrefFunction(context)
        with(sharedPref.editor) {
            putInt(SharedPrefFunction.SPECIFIC_HOUR, hourOfDay)
            putInt(SharedPrefFunction.SPECIFIC_MINUTE, minute)
            putBoolean(SharedPrefFunction.TIMER_TYPE, false)
            apply()
        }

        _isTimerType.value = false
        updateScheduleText(context)
    }

    fun dismissMenuDialog(fragment: Fragment) {
        val prevFragment = fragment.parentFragmentManager
            .findFragmentByTag("ScheduleMenuFragment")
        if (prevFragment != null) {
            val prevDialog = prevFragment as DialogFragment
            prevDialog.dismiss()
        }
    }

    private fun updateScheduleText(context: Context) {
        when (_isTimerType.value) {
            true -> {
                val hour: Int = _relativeHourNum.value ?: 0
                val minute: Int =  _relativeMinuteNum.value ?: 0
                val second: Int = _relativeSecondNum.value ?: 0

                if (hour == 0 && minute == 0 && second == 0) {
                    _scheduleText.value = "Now"
                } else {
                    _scheduleText.value = _relativeTimeText.value
                }
            }
            false -> {
                // Get current time from phone
                val c: Calendar = Calendar.getInstance()
                val currentHour = c.get(Calendar.HOUR_OF_DAY)
                val currentMinute = c.get(Calendar.MINUTE)
                val currentMinuteOfDay = (currentHour*60) + currentMinute

                // Get active/assigned time from dialog
                val assignedHourOfDay: Int = _specificHourNum.value ?: 0
                val assignedMinute: Int = _specificMinuteNum.value ?: 0
                val assignedMinuteOfDay = (assignedHourOfDay*60) + assignedMinute

                // Set padding for number
                val assignedHourPad: String =
                    assignedHourOfDay.toString().padStart(2, '0')
                val assignedMinutePad: String =
                    assignedMinute.toString().padStart(2, '0')

                // Set _scheduleText according to choice
                if (assignedMinuteOfDay <= currentMinuteOfDay) {
                    _scheduleText.value = "Now"
                } else {
                    _scheduleText.value = "$assignedHourPad:$assignedMinutePad"
                }
            }
        }
        val sharedPref = SharedPrefFunction(context)
        with(sharedPref.editor) {
            putString(SharedPrefFunction.SCHEDULE_TEXT, _scheduleText.value)
            apply()
        }
    }
}