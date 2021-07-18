package com.sawelo.infake

import android.content.Context
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.infake.databinding.DialogMenuScheduleBinding
import com.sawelo.infake.databinding.DialogRelativeScheduleBinding
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*

class CreateViewModel : ViewModel() {
    private val _mainScheduleText = MutableLiveData<String>()
    val mainScheduleText: LiveData<String> = _mainScheduleText

    private val _relativeTimeText = MutableLiveData<String>()
    val relativeTimeText: LiveData<String> = _relativeTimeText

    private val _relativeHourNum = MutableLiveData<Int>()
    val relativeHourNum: LiveData<Int> = _relativeHourNum
    private val _relativeMinuteNum = MutableLiveData<Int>()
    val relativeMinuteNum: LiveData<Int> = _relativeMinuteNum
    private val _relativeSecondNum = MutableLiveData<Int>()
    val relativeSecondNum: LiveData<Int> = _relativeSecondNum

    private val _specificHourNum = MutableLiveData<Int>()
    val specificHourNum: LiveData<Int> = _specificHourNum
    private val _specificMinuteNum = MutableLiveData<Int>()
    val specificMinuteNum: LiveData<Int> = _specificMinuteNum

    private val _isTimerType = MutableLiveData<Boolean>()

    private val _menuArray = MutableLiveData<List<MenuData>>()

    init {
        _mainScheduleText.value = "Schedule Call"
        _relativeTimeText.value = "Set timer"

        _relativeHourNum.value = 0
        _relativeMinuteNum.value = 0
        _relativeSecondNum.value = 0

        _specificHourNum.value = 0
        _specificMinuteNum.value = 0

        _isTimerType.value = true

        _menuArray.value = mutableListOf(
            MenuData(true, minute = 0),
            MenuData(true, minute = 2),
            MenuData(true, minute = 5),
            MenuData(true, minute = 10)
        )
    }

    fun menuButton(
        context: Context,
        fragment: Fragment,
        binding: DialogMenuScheduleBinding,
    ) {
        _menuArray.value?.forEachIndexed { i, menuData ->
            val button = when (i) {
                0 -> binding.button1
                1 -> binding.button2
                2 -> binding.button3
                else -> binding.button4
            }
            button.setOnClickListener {
                mainSetTime(context, menuData)
                dismissMenuDialog(fragment)
            }
            button.text = mainUpdateText(context, menuData, withSharedPref = false)
        }
    }

    fun mainSetTime(context: Context, menuData: MenuData) {
        val hour: Int = menuData.hour ?: 0
        val minute: Int = menuData.minute ?: 0
        val second: Int = menuData.second ?: 0

        if (menuData.timerType == true) {
            val sharedPref = SharedPrefFunction(context)
            with(sharedPref.editor) {
                putInt(SharedPrefFunction.RELATIVE_HOUR, hour)
                putInt(SharedPrefFunction.RELATIVE_MINUTE, minute)
                putInt(SharedPrefFunction.RELATIVE_SECOND, second)
                putBoolean(SharedPrefFunction.TIMER_TYPE, true)
                apply()
            }
            mainUpdateText(context, menuData)
        } else {
            val sharedPref = SharedPrefFunction(context)
            with(sharedPref.editor) {
                putInt(SharedPrefFunction.SPECIFIC_HOUR, hour)
                putInt(SharedPrefFunction.SPECIFIC_MINUTE, minute)
                putBoolean(SharedPrefFunction.TIMER_TYPE, false)
                apply()
            }
            mainUpdateText(context, menuData)
        }
    }

    fun updateRelativeTime(
        picker: NumberPicker?,
        binding: DialogRelativeScheduleBinding,
        newVal: Int
    ) {
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
            _relativeSecondNum.value == 0
        ) {
            _relativeTimeText.value = "Set timer"
        } else {
            _relativeTimeText.value = "$hourPad:$minutePad:$secondPad"
        }
    }

    fun updateSpecificTime(hourOfDay: Int, minute: Int) {
        _specificHourNum.value = hourOfDay
        _specificMinuteNum.value = minute
    }

    fun dismissMenuDialog(fragment: Fragment) {
        val prevFragment = fragment.parentFragmentManager
            .findFragmentByTag("ScheduleMenuFragment")
        if (prevFragment != null) {
            val prevDialog = prevFragment as DialogFragment
            prevDialog.dismiss()
        }
    }

    private fun mainUpdateText(context: Context, menuData: MenuData, withSharedPref: Boolean = true): String {
        // Second int is optional since it's useless for alarm type

        fun adjustNum(numValue: Int?, singular: String, plural: String): String {
            return when (numValue) {
                0 -> ""
                1 -> "1 $singular"
                else -> "$numValue $plural"
            }
        }

        var mainScheduleText = ""
        val displayText: String

        when (menuData.timerType) {
            true -> {
                // This will return text for timer type
                val thisHour: String = adjustNum(menuData.hour, "hour", "hours")
                val thisMinute = adjustNum(menuData.minute, "minute", "minutes")
                val thisSecond = adjustNum(menuData.second, "second", "seconds")

                // Checks if all val is not blank
                displayText =
                    if (thisHour.isNotBlank() && thisMinute.isNotBlank() && thisSecond.isNotBlank()) {
                        // Use already existing data with updateRelativeTime()
                        "${_relativeTimeText.value}"
                    } else {
                        val builder = StringBuilder()
                        if (thisHour.isNotBlank()) builder.append(thisHour).append(" ")
                        if (thisMinute.isNotBlank()) builder.append(thisMinute).append(" ")
                        if (thisSecond.isNotBlank()) builder.append(thisSecond).append(" ")
                        builder.toString()
                    }

                // Set now or else
                mainScheduleText =
                    if (menuData.hour == 0 && menuData.minute == 0 && menuData.second == 0) {
                        "Now"
                    } else {
                        displayText
                    }
            }
            false -> {
                val hourPad: String = menuData.hour.toString().padStart(2, '0')
                val minutePad: String = menuData.minute.toString().padStart(2, '0')
                displayText = "$hourPad:$minutePad"

                // Get current time from phone
                val c: Calendar = Calendar.getInstance()
                val currentHour = c.get(Calendar.HOUR_OF_DAY)
                val currentMinute = c.get(Calendar.MINUTE)
                val currentMinuteOfDay = (currentHour * 60) + currentMinute

                // Get active/assigned time from dialog
                val assignedHourOfDay: Int = menuData.hour ?: 0
                val assignedMinute: Int = menuData.minute ?: 0
                val assignedMinuteOfDay = (assignedHourOfDay * 60) + assignedMinute

                // Set now or else
                mainScheduleText =
                    if (assignedMinuteOfDay <= currentMinuteOfDay) {
                        "Now"
                    } else {
                        displayText
                    }
            }
        }
        println("mainScheduleText is $mainScheduleText")

        if (withSharedPref) {
            _mainScheduleText.value = mainScheduleText
            val sharedPref = SharedPrefFunction(context)
            with(sharedPref.editor) {
                putString(SharedPrefFunction.SCHEDULE_TEXT, _mainScheduleText.value)
                apply()
            }
        }

        return mainScheduleText
    }
}